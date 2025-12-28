package floorida.example.floorida.team.controller;

import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.entity.TeamMember;
import floorida.example.floorida.team.repository.TeamMemberRepository;
import floorida.example.floorida.team.repository.TeamRepository;
import floorida.example.floorida.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;



    // 1) 팀 생성
    @PostMapping
    public ResponseEntity<CreateTeamResponse> create(@RequestBody CreateTeamRequest req) {
        Long teamId = teamService.createTeam(req.userId(), req.name(), req.description());

        // 생성 후 joinCode를 응답으로 주면 테스트가 편함
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalStateException("team not found"));

        return ResponseEntity.ok(new CreateTeamResponse(teamId, team.getJoinCode()));
    }

    // 2) 팀 가입 (초대코드)
    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody JoinTeamRequest req) {
        teamService.joinTeam(req.userId(), req.joinCode());
        return ResponseEntity.ok().build();
    }

    // 3) 팀 멤버 목록 조회 (테스트용)
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberDto>> members(@PathVariable Long teamId) {
        List<TeamMember> list = teamMemberRepository.findByTeam_Id(teamId);
        return ResponseEntity.ok(
                list.stream().map(TeamMemberDto::from).toList()
        );
    }

    // 4) 권한 체크 테스트용 엔드포인트 (owner/admin만 200)
    @GetMapping("/{teamId}/admin-check")
    public ResponseEntity<String> adminCheck(@PathVariable Long teamId,
                                             @RequestParam Long userId) {
        teamService.validateAdmin(teamId, userId);
        return ResponseEntity.ok("OK");
    }

    // ===== DTOs =====
    public record CreateTeamRequest(Long userId, String name, String description) {}
    public record CreateTeamResponse(Long teamId, String joinCode) {}
    public record JoinTeamRequest(Long userId, String joinCode) {}

    public record TeamMemberDto(Long teamMemberId, Long userId, String role) {
        static TeamMemberDto from(TeamMember tm) {
            return new TeamMemberDto(
                    tm.getId(),
                    tm.getUser().getUserId(), // User 엔티티 PK 이름이 userId라고 하셨으니
                    tm.getRole()
            );
        }
    }
}