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

        return teamFloorRepository.findByTeam_IdOrderByDueDateAscCreatedAtAsc(teamId)
                .stream()
                .map(f -> new TeamFloorResponse(
                        f.getId(),
                        f.getTeam().getId(),
                        f.getTitle(),
                        f.getDueDate(),
                        f.isCompleted(),
                        f.getCompletedAt(),
                        teamFloorStatusRepository.findByIdTeamFloorId(f.getId())
                                .stream()
                                .map(s -> s.getUser().getUserId())
                                .toList()
                ))
                .toList();
    }

    /* =========================================================
       3) 할 일 상세 조회 (member)
       ========================================================= */
    @Transactional(readOnly = true)
    public TeamFloorDetailResponse getTeamFloorDetail(Long requesterUserId, Long teamId, Long teamFloorId) {
        teamService.getMember(teamId, requesterUserId);

        TeamFloor floor = teamFloorRepository.findByIdAndTeam_Id(teamFloorId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        return new TeamFloorDetailResponse(
                floor.getId(),
                floor.getTeam().getId(),
                floor.getTitle(),
                floor.getDueDate(),
                floor.isCompleted(),
                floor.getCompletedAt(),
                teamFloorStatusRepository.findByIdTeamFloorId(teamFloorId)
                        .stream()
                        .map(s -> s.getUser().getUserId())
                        .toList()
        );
    }

    /* =========================================================
       4) 할 일 수정 (owner)
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

        // TeamFloor 엔티티에 setter가 없다면, setter 추가하거나 update 메서드로 바꾸면 됨.
        floor.setTitle(req.getTitle());
        floor.setDueDate(dueDate);

        // dirty checking으로 자동 반영
    }

    /* =========================================================
       5) 할 일 삭제 (owner)
       - 배정 매핑 먼저 삭제 후 할 일 삭제
       ========================================================= */
    public void deleteTeamFloor(Long requesterUserId, Long teamFloorId) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();
        teamService.validateOwner(teamId, requesterUserId);

        // FK 고려: 상태(매핑) 먼저 삭제
        teamFloorStatusRepository.deleteByIdTeamFloorId(teamFloorId);
        teamFloorRepository.delete(floor);
    }

    /* =========================================================
       6) 배정자 변경 (owner)
       - 빈 배열 허용: 미정으로 만들기 가능
       - 기존 배정 매핑 삭제 후 새로 insert
       ========================================================= */
    public void updateAssignees(Long requesterUserId, Long teamFloorId, TeamFloorAssigneesUpdateRequest req) {
        TeamFloor floor = teamFloorRepository.findById(teamFloorId)
                .orElseThrow(() -> new EntityNotFoundException("TeamFloor not found"));

        Long teamId = floor.getTeam().getId();
        teamService.validateOwner(teamId, requesterUserId);

        // 기존 매핑 삭제
        teamFloorStatusRepository.deleteByIdTeamFloorId(teamFloorId);

        List<Long> newIds = req.getAssigneeUserIds() == null
                ? Collections.emptyList()
                : req.getAssigneeUserIds();

        for (Long uid : newIds.stream().distinct().toList()) {
            // 팀 멤버인지 검증
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
       7) 완료 처리 (배정자 OR 팀장)
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

        if (floor.isCompleted()) {
            return new CompleteResult(true, false);
        }

        Instant now = Instant.now();

        if (isAssignee) {
            teamFloorStatusRepository
                    .markAssigneeCompletedIfNotCompleted(teamFloorId, requesterUserId, now);
        }

        if (teamFloorRepository.markCompletedIfNotCompleted(teamFloorId, now) == 1) {
            teamRepository.incrementLevel(teamId);
            return new CompleteResult(false, true);
        }

        return new CompleteResult(true, false);
    }

    /* =========================================================
       8) 완료 취소 (배정자 OR 팀장)
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

        if (!floor.isCompleted()) {
            return new CancelResult(true, false);
        }

        if (teamFloorRepository.cancelIfCompleted(teamFloorId) == 1) {
            teamFloorStatusRepository.resetAllAssigneesStatus(teamFloorId);
            teamRepository.decrementLevel(teamId);
            return new CancelResult(false, true);
        }

        return new CancelResult(true, false);
    }

    @Getter
    @AllArgsConstructor
    public static class CompleteResult {
        private boolean alreadyCompleted;
        private boolean levelUp;
    }

    @Getter
    @AllArgsConstructor
    public static class CancelResult {
        private boolean alreadyIncomplete;
        private boolean levelDown;
    }
}

