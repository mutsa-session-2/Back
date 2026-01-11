package floorida.example.floorida.team.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.service.UserProfileService;
import floorida.example.floorida.team.dto.*;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.entity.TeamFloor;
import floorida.example.floorida.team.entity.TeamFloorStatus;
import floorida.example.floorida.team.entity.TeamFloorStatusId;
import floorida.example.floorida.team.repository.TeamFloorRepository;
import floorida.example.floorida.team.repository.TeamFloorStatusRepository;
import floorida.example.floorida.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamFloorService {

    private final TeamService teamService;
    private final TeamRepository teamRepository;
    private final TeamFloorRepository teamFloorRepository;
    private final TeamFloorStatusRepository teamFloorStatusRepository;
    private final UserRepository userRepository;

    // ✅ 개인 코인/레벨 관리용 서비스 (user_profiles)
    private final UserProfileService userProfileService;

    /* =========================================================
       1) 할 일 생성 (admin/owner)
       - 담당자: 0명 또는 1명만 허용
       ========================================================= */
    public Long createTeamFloor(Long requesterUserId, Long teamId, TeamFloorCreateRequest req) {
        // OWNER/ADMIN 권한 체크
        teamService.validateAdmin(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);

        LocalDate dueDate = req.getDueDate();
        if (dueDate != null) {
            if (dueDate.isBefore(team.getStartDate()) || dueDate.isAfter(team.getEndDate())) {
                throw new IllegalArgumentException("dueDate out of team period");
            }
        }

        TeamFloor floor = TeamFloor.builder()
                .team(team)
                .title(req.getTitle())
                .dueDate(dueDate)
                .completed(false)
                .completedAt(null)
                .build();

        TeamFloor saved = teamFloorRepository.save(floor);

        // ⭐ 담당자는 한 명만 허용
        List<Long> assigneeIds = (req.getAssigneeUserIds() == null)
                ? Collections.emptyList()
                : req.getAssigneeUserIds();

        if (assigneeIds.size() > 1) {
            throw new IllegalArgumentException("담당자는 한 명만 지정할 수 있습니다.");
        }

        if (!assigneeIds.isEmpty()) {
            Long assigneeId = assigneeIds.get(0);

            // 팀 멤버인지 검증
            teamService.getMember(teamId, assigneeId);

            User user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new EntityNotFoundException("user not found"));

            teamFloorStatusRepository.save(
                    TeamFloorStatus.builder()
                            .id(new TeamFloorStatusId(saved.getId(), assigneeId))
                            .teamFloor(saved)
                            .user(user)
                            .completed(false)
                            .completedAt(null)
                            .coinsAwarded(0)
                            .build()
            );
        }

        return saved.getId();
    }

    /* =========================================================
       2) 팀 할 일 목록 조회 (member)
       - 할 일이 0개여도 teamLevel 내려주기 위해 Wrapper 사용
       ========================================================= */
    @Transactional(readOnly = true)
    public TeamFloorListResponse listTeamFloors(Long requesterUserId, Long teamId) {
        teamService.getMember(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);
        Integer teamLevel = team.getLevel();

        List<TeamFloor> floors = teamFloorRepository.findByTeam_IdOrderByDueDateAscCreatedAtAsc(teamId);
        if (floors.isEmpty()) {
            return new TeamFloorListResponse(teamLevel, List.of());
        }

        List<Long> floorIds = floors.stream().map(TeamFloor::getId).toList();

        // 한번에 status 조회 (N+1 방지)
        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorIdIn(floorIds);

        Map<Long, List<TeamFloorStatus>> statusByFloorId =
                statuses.stream().collect(Collectors.groupingBy(s -> s.getTeamFloor().getId()));

        List<TeamFloorResponse> items = floors.stream()
                .map(f -> {
                    List<TeamFloorStatus> ss = statusByFloorId.getOrDefault(f.getId(), Collections.emptyList());

                    List<Long> assigneeUserIds = ss.stream()
                            .map(s -> s.getUser().getUserId())
                            .toList();

                    List<TeamFloorResponse.AssigneeInfo> assignees = ss.stream()
                            .map(s -> new TeamFloorResponse.AssigneeInfo(
                                    s.getUser().getUserId(),
                                    s.getUser().getUsername()
                            ))
                            .toList();

                    return new TeamFloorResponse(
                            f.getId(),
                            f.getTeam().getId(),
                            f.getTitle(),
                            f.getDueDate(),
                            f.isCompleted(),
                            f.getCompletedAt(),
                            assigneeUserIds,
                            assignees,
                            teamLevel
                    );
                })
                .toList();

        return new TeamFloorListResponse(teamLevel, items);
    }

    /* =========================================================
       3) 팀 "미완료" 할 일 목록 조회 (member)
       - 할 일이 0개여도 teamLevel 내려주기 위해 Wrapper 사용
       ========================================================= */
    @Transactional(readOnly = true)
    public TeamFloorListResponse listIncompleteTeamFloors(Long requesterUserId, Long teamId) {
        teamService.getMember(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);
        Integer teamLevel = team.getLevel();

        List<TeamFloor> floors = teamFloorRepository.findByTeam_IdAndCompletedFalseOrderByDueDateAscCreatedAtAsc(teamId);
        if (floors.isEmpty()) {
            return new TeamFloorListResponse(teamLevel, List.of());
        }

        List<Long> floorIds = floors.stream().map(TeamFloor::getId).toList();
        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorIdIn(floorIds);

        Map<Long, List<TeamFloorStatus>> statusByFloorId =
                statuses.stream().collect(Collectors.groupingBy(s -> s.getTeamFloor().getId()));

        List<TeamFloorResponse> items = floors.stream()
                .map(f -> {
                    List<TeamFloorStatus> ss = statusByFloorId.getOrDefault(f.getId(), Collections.emptyList());

                    List<Long> assigneeUserIds = ss.stream()
                            .map(s -> s.getUser().getUserId())
                            .toList();

                    List<TeamFloorResponse.AssigneeInfo> assignees = ss.stream()
                            .map(s -> new TeamFloorResponse.AssigneeInfo(
                                    s.getUser().getUserId(),
                                    s.getUser().getUsername()
                            ))
                            .toList();

                    return new TeamFloorResponse(
                            f.getId(),
                            f.getTeam().getId(),
                            f.getTitle(),
                            f.getDueDate(),
                            f.isCompleted(),
                            f.getCompletedAt(),
                            assigneeUserIds,
                            assignees,
                            teamLevel
                    );
                })
                .toList();

        return new TeamFloorListResponse(teamLevel, items);
    }

    /* =========================================================
       4) 할 일 상세 조회 (member)
       ========================================================= */
    @Transactional(readOnly = true)
    public TeamFloorDetailResponse getTeamFloorDetail(Long requesterUserId, Long teamId, Long teamFloorId) {
        teamService.getMember(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);
        Integer teamLevel = team.getLevel();

        TeamFloor floor = teamFloorRepository.findByIdAndTeam_Id(teamFloorId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        List<TeamFloorStatus> statuses =
                teamFloorStatusRepository.findByIdTeamFloorId(teamFloorId);

        List<Long> assigneeUserIds = statuses.stream()
                .map(s -> s.getUser().getUserId())
                .toList();

        List<TeamFloorDetailResponse.AssigneeInfo> assignees = statuses.stream()
                .map(s -> new TeamFloorDetailResponse.AssigneeInfo(
                        s.getUser().getUserId(),
                        s.getUser().getUsername()
                ))
                .toList();

        return new TeamFloorDetailResponse(
                floor.getId(),
                floor.getTeam().getId(),
                floor.getTitle(),
                floor.getDueDate(),
                floor.isCompleted(),
                floor.getCompletedAt(),
                assigneeUserIds,
                assignees,
                teamLevel
        );
    }

    /* =========================================================
       5) 할 일 수정 (owner)
       ========================================================= */
    public void updateTeamFloor(Long requesterUserId, Long teamFloorId, TeamFloorUpdateRequest req) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();
        teamService.validateOwner(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);

        LocalDate dueDate = req.getDueDate();
        if (dueDate != null) {
            if (dueDate.isBefore(team.getStartDate()) || dueDate.isAfter(team.getEndDate())) {
                throw new IllegalArgumentException("dueDate out of team period");
            }
        }

        floor.setTitle(req.getTitle());
        floor.setDueDate(dueDate);
    }

    /* =========================================================
       6) 할 일 삭제 (owner)
       ========================================================= */
    public void deleteTeamFloor(Long requesterUserId, Long teamFloorId) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();
        teamService.validateOwner(teamId, requesterUserId);

        teamFloorStatusRepository.deleteByIdTeamFloorId(teamFloorId);
        teamFloorRepository.delete(floor);
    }

    /* =========================================================
       7) 배정자 변경 (owner)
       - 담당자: 0명 또는 1명만 허용
       ========================================================= */
    public void updateAssignees(Long requesterUserId, Long teamFloorId, TeamFloorAssigneesUpdateRequest req) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();
        teamService.validateOwner(teamId, requesterUserId);

        List<Long> newIds = (req.getAssigneeUserIds() == null)
                ? Collections.emptyList()
                : req.getAssigneeUserIds();

        if (newIds.size() > 1) {
            throw new IllegalArgumentException("담당자는 한 명만 지정할 수 있습니다.");
        }

        // 기존 배정자 전부 삭제
        teamFloorStatusRepository.deleteByIdTeamFloorId(teamFloorId);

        if (!newIds.isEmpty()) {
            Long uid = newIds.get(0);

            teamService.getMember(teamId, uid);

            User user = userRepository.findById(uid)
                    .orElseThrow(() -> new EntityNotFoundException("user not found"));

            teamFloorStatusRepository.save(
                    TeamFloorStatus.builder()
                            .id(new TeamFloorStatusId(teamFloorId, uid))
                            .teamFloor(floor)
                            .user(user)
                            .completed(false)
                            .completedAt(null)
                            .coinsAwarded(0)
                            .build()
            );
        }
    }

    /* =========================================================
       8) 완료 처리 (배정자 OR owner/admin)
       - 코인 정책:
         - 오늘/미래 dueDate: +10코인
         - 과거 dueDate(지각): +0코인
       - 코인은 항상 "배정자" 개인 프로필(UserProfile.points)에 쌓임
       ========================================================= */
    public CompleteResult complete(Long requesterUserId, Long teamFloorId) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();

        boolean isAssignee =
                teamFloorStatusRepository.existsByIdTeamFloorIdAndIdUserId(teamFloorId, requesterUserId);

        if (!isAssignee) {
            // 배정자가 아니면 OWNER/ADMIN 권한 체크
            teamService.validateAdmin(teamId, requesterUserId);
        }

        // 이미 완료된 경우: 코인/레벨 변화 없음
        if (floor.isCompleted()) {
            Integer level = teamRepository.findLevelById(teamId);
            return new CompleteResult(true, false, level, 0, false);
        }

        // 지각 여부 계산
        LocalDate today = LocalDate.now();
        LocalDate dueDate = floor.getDueDate();
        boolean late = (dueDate != null && dueDate.isBefore(today));

        int baseCoins = late ? 0 : 10;

        // 담당자 상태 가져오기 (단일 담당자 전제, 과거 데이터에 여러 명 있어도 첫 번째만 사용)
        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorId(teamFloorId);
        TeamFloorStatus assigneeStatus = statuses.stream().findFirst().orElse(null);

        Instant now = Instant.now();
        int coinsAwarded = 0;

        if (assigneeStatus != null && !assigneeStatus.isCompleted()) {
            assigneeStatus.setCompleted(true);
            assigneeStatus.setCompletedAt(now);
            assigneeStatus.setCoinsAwarded(baseCoins);

            if (baseCoins > 0) {
                Long assigneeUserId = assigneeStatus.getUser().getUserId();
                userProfileService.addPoints(assigneeUserId, baseCoins);
                coinsAwarded = baseCoins;
            }
        } else {
            // 담당자가 없거나 이미 완료된 상태라면 코인 X, late 의미도 거의 없음
            late = false;
        }

        boolean levelUp = false;
        if (teamFloorRepository.markCompletedIfNotCompleted(teamFloorId, now) == 1) {
            teamRepository.incrementLevel(teamId);
            levelUp = true;
        }

        Integer level = teamRepository.findLevelById(teamId);
        return new CompleteResult(false, levelUp, level, coinsAwarded, late);
    }

    /* =========================================================
       9) 완료 취소 (배정자 OR owner/admin)
       - 코인 정책:
         - 정상 완료(+10)이었던 건 취소: -10코인
         - 지각 완료(+0)이었던 건 취소: 0코인
       - 항상 "배정자"에게서 회수
       ========================================================= */
    public CancelResult cancel(Long requesterUserId, Long teamFloorId) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();

        boolean isAssignee =
                teamFloorStatusRepository.existsByIdTeamFloorIdAndIdUserId(teamFloorId, requesterUserId);

        if (!isAssignee) {
            // 배정자가 아니면 owner 권한 체크
            teamService.validateAdmin(teamId, requesterUserId);
        }

        // 이미 미완료 상태면 아무 변화 없음
        if (!floor.isCompleted()) {
            Integer level = teamRepository.findLevelById(teamId);
            return new CancelResult(true, false, level, 0);
        }

        // 담당자 상태
        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorId(teamFloorId);
        TeamFloorStatus assigneeStatus = statuses.stream().findFirst().orElse(null);

        int coinsDeducted = 0;

        //담당자가 있다면 (만약 없을시 코인 지급 관련 로직은 건너뜀)
        if (assigneeStatus != null) {
            int awarded = assigneeStatus.getCoinsAwarded(); // 10 or 0(마감일 지난 경우)

            if (awarded > 0) {
                Long assigneeUserId = assigneeStatus.getUser().getUserId();
                userProfileService.deductPoints(assigneeUserId, awarded);
                coinsDeducted = awarded;
            }

            assigneeStatus.setCoinsAwarded(0);
            assigneeStatus.setCompleted(false);
            assigneeStatus.setCompletedAt(null);
        }

        boolean levelDown = false;
        if (teamFloorRepository.cancelIfCompleted(teamFloorId) == 1) {
            teamRepository.decrementLevel(teamId);
            levelDown = true;
        }

        Integer level = teamRepository.findLevelById(teamId);
        return new CancelResult(false, levelDown, level, coinsDeducted);
    }

    @Getter
    @AllArgsConstructor
    public static class CompleteResult {
        private boolean alreadyCompleted;
        private boolean levelUp;
        private Integer teamLevel;

        // 이번 완료로 실제 지급된 코인 (10 또는 0)
        private int coinsAwarded;

        // 지각 여부 (dueDate < today일 때 true)
        private boolean late;
    }

    @Getter
    @AllArgsConstructor
    public static class CancelResult {
        private boolean alreadyIncomplete;
        private boolean levelDown;
        private Integer teamLevel;

        // 이번 취소로 실제 회수된 코인 (10 또는 0)
        private int coinsDeducted;
    }
}
