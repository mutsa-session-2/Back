package floorida.example.floorida.team.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    /* =========================================================
       1) 할 일 생성 (admin/owner)
       ========================================================= */
    public Long createTeamFloor(Long requesterUserId, Long teamId, TeamFloorCreateRequest req) {
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

        List<Long> assigneeIds = req.getAssigneeUserIds() == null
                ? Collections.emptyList()
                : req.getAssigneeUserIds();

        for (Long uid : assigneeIds.stream().distinct().toList()) {
            teamService.getMember(teamId, uid);

            User user = userRepository.findById(uid)
                    .orElseThrow(() -> new EntityNotFoundException("user not found"));

            teamFloorStatusRepository.save(
                    TeamFloorStatus.builder()
                            .id(new TeamFloorStatusId(saved.getId(), uid))
                            .teamFloor(saved)
                            .user(user)
                            .completed(false)
                            .completedAt(null)
                            .build()
            );
        }

        return saved.getId();
    }

    /* =========================================================
       2) 팀 할 일 목록 조회 (member)
       - 배정자 없는 경우 assigneeUserIds == [] (미정)
       ========================================================= */
    @Transactional(readOnly = true)
    public List<TeamFloorResponse> listTeamFloors(Long requesterUserId, Long teamId) {
        teamService.getMember(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);
        Integer teamLevel = team.getLevel();

        List<TeamFloor> floors = teamFloorRepository.findByTeam_IdOrderByDueDateAscCreatedAtAsc(teamId);
        if (floors.isEmpty()) return Collections.emptyList();

        List<Long> floorIds = floors.stream().map(TeamFloor::getId).toList();

        // 한번에 status 조회 (N+1 방지)
        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorIdIn(floorIds);

        Map<Long, List<TeamFloorStatus>> statusByFloorId =
                statuses.stream().collect(Collectors.groupingBy(s -> s.getTeamFloor().getId()));

        return floors.stream()
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
    }

    /* =========================================================
       3) 팀 "미완료" 할 일 목록 조회 (member)
       - completed=false만 모아서 보기
       ========================================================= */
    @Transactional(readOnly = true)
    public List<TeamFloorResponse> listIncompleteTeamFloors(Long requesterUserId, Long teamId) {
        teamService.getMember(teamId, requesterUserId);

        Team team = teamService.getTeamOrThrow(teamId);
        Integer teamLevel = team.getLevel();

        List<TeamFloor> floors = teamFloorRepository.findByTeam_IdAndCompletedFalseOrderByDueDateAscCreatedAtAsc(teamId);
        if (floors.isEmpty()) return Collections.emptyList();

        List<Long> floorIds = floors.stream().map(TeamFloor::getId).toList();
        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorIdIn(floorIds);

        Map<Long, List<TeamFloorStatus>> statusByFloorId =
                statuses.stream().collect(Collectors.groupingBy(s -> s.getTeamFloor().getId()));

        return floors.stream()
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

        List<TeamFloorStatus> statuses = teamFloorStatusRepository.findByIdTeamFloorId(teamFloorId);

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
       - 제목/마감일 수정
       - dueDate는 팀 프로젝트 기간 내
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
       - 배정 매핑 먼저 삭제 후 할 일 삭제
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
       - 빈 배열 허용: 미정으로 만들기 가능
       ========================================================= */
    public void updateAssignees(Long requesterUserId, Long teamFloorId, TeamFloorAssigneesUpdateRequest req) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();
        teamService.validateOwner(teamId, requesterUserId);

        teamFloorStatusRepository.deleteByIdTeamFloorId(teamFloorId);

        List<Long> newIds = req.getAssigneeUserIds() == null
                ? Collections.emptyList()
                : req.getAssigneeUserIds();

        for (Long uid : newIds.stream().distinct().toList()) {
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
                            .build()
            );
        }
    }

    /* =========================================================
       8) 완료 처리 (배정자 OR 팀장)
       ========================================================= */
    public CompleteResult complete(Long requesterUserId, Long teamFloorId) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();

        boolean isAssignee =
                teamFloorStatusRepository.existsByIdTeamFloorIdAndIdUserId(teamFloorId, requesterUserId);

        if (!isAssignee) {
            teamService.validateAdmin(teamId, requesterUserId);
        }

        // 이미 완료된 경우
        if (floor.isCompleted()) {
            Integer level = teamRepository.findLevelById(teamId);
            return new CompleteResult(true, false, level);
        }

        Instant now = Instant.now();

        if (isAssignee) {
            teamFloorStatusRepository
                    .markAssigneeCompletedIfNotCompleted(teamFloorId, requesterUserId, now);
        }

        // 처음 완료 성공
        if (teamFloorRepository.markCompletedIfNotCompleted(teamFloorId, now) == 1) {
            teamRepository.incrementLevel(teamId);
            Integer level = teamRepository.findLevelById(teamId);
            return new CompleteResult(false, true, level);
        }

        Integer level = teamRepository.findLevelById(teamId);
        return new CompleteResult(true, false, level);
    }


    /* =========================================================
       9) 완료 취소 (배정자 OR 팀장)
       ========================================================= */
    public CancelResult cancel(Long requesterUserId, Long teamFloorId) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();

        boolean isAssignee =
                teamFloorStatusRepository.existsByIdTeamFloorIdAndIdUserId(teamFloorId, requesterUserId);

        if (!isAssignee) {
            teamService.validateAdmin(teamId, requesterUserId);
        }

        // 이미 미완료
        if (!floor.isCompleted()) {
            Integer level = teamRepository.findLevelById(teamId);
            return new CancelResult(true, false, level);
        }

        if (teamFloorRepository.cancelIfCompleted(teamFloorId) == 1) {
            teamFloorStatusRepository.resetAllAssigneesStatus(teamFloorId);
            teamRepository.decrementLevel(teamId);

            Integer level = teamRepository.findLevelById(teamId);
            return new CancelResult(false, true, level);
        }

        Integer level = teamRepository.findLevelById(teamId);
        return new CancelResult(true, false, level);
    }


    @Getter
    @AllArgsConstructor
    public static class CompleteResult {
        private boolean alreadyCompleted;
        private boolean levelUp;
        private Integer teamLevel;
    }

    @Getter
    @AllArgsConstructor
    public static class CancelResult {
        private boolean alreadyIncomplete;
        private boolean levelDown;
        private Integer teamLevel;
    }
}
