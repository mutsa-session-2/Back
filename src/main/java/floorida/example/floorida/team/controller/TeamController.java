package floorida.example.floorida.team.controller;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.service.CurrentUserService;
import floorida.example.floorida.team.dto.*;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.repository.TeamMemberRepository;
import floorida.example.floorida.team.repository.TeamRepository;
import floorida.example.floorida.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Validated
public class TeamController {

    private final TeamService teamService;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CurrentUserService currentUserService;

    private User meOrThrow() {
        return currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("unauthorized"));
    }

    // 1) 팀 생성
    @PostMapping
    public ResponseEntity<TeamCreateResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        User me = meOrThrow();

        // null description 넘기지 않도록 (TeamService 오버로드 사용 권장)
        Long teamId = teamService.createTeam(
                me.getUserId(),
                request.getName(),
                request.getStartDate(),
                request.getEndDate()
        );

        Team team = teamService.getTeamOrThrow(teamId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TeamCreateResponse(teamId, team.getJoinCode()));
    }

    // 2) 팀 가입 (초대코드)
    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(@Valid @RequestBody TeamJoinRequest request) {
        User me = meOrThrow();
        teamService.joinTeam(me.getUserId(), request.getJoinCode());
        return ResponseEntity.ok().build();
    }

    // 3) 팀 멤버 목록 조회 (userId + role)
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberResponse>> getMembers(@PathVariable Long teamId) {
        User me = meOrThrow();

        // 팀 존재 확인 (404용)
        teamService.getTeamOrThrow(teamId);

        // 멤버만 접근 가능 (403용)
        teamService.getMember(teamId, me.getUserId());

        List<Object[]> rows = teamMemberRepository.findUserIdAndRoleByTeamId(teamId);

        List<TeamMemberResponse> result = rows.stream()
                .map(r -> new TeamMemberResponse((Long) r[0], (String) r[1]))
                .toList();

        return ResponseEntity.ok(result);
    }

    // 4) 내 팀 목록 조회
    @GetMapping
    public ResponseEntity<List<MyTeamResponse>> myTeams() {
        User me = meOrThrow();

        List<Long> teamIds = teamMemberRepository.findTeamIdsByUserId(me.getUserId());
        if (teamIds.isEmpty()) return ResponseEntity.ok(List.of());

        List<Team> teams = teamRepository.findByIdIn(teamIds);

        List<MyTeamResponse> result = teams.stream()
                .map(t -> new MyTeamResponse(
                        t.getId(),
                        t.getName(),
                        t.getStartDate(),
                        t.getEndDate()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    // 5) 팀 단건 조회 (팀 기본 정보)
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable Long teamId) {
        User me = meOrThrow();

        // 팀 존재 확인 (404용)
        Team team = teamService.getTeamOrThrow(teamId);

        // 팀 멤버만 조회 가능 (403용)
        teamService.getMember(teamId, me.getUserId());

        return ResponseEntity.ok(new TeamResponse(
                team.getId(),
                team.getName(),
                team.getLevel(),
                team.getStartDate(),
                team.getEndDate(),
                team.getCreatedAt()
        ));
    }
}



