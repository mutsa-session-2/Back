package floorida.example.floorida.team.controller;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.service.CurrentUserService;
import floorida.example.floorida.team.dto.*;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.entity.TeamMember;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Validated
@Tag(name = "팀", description = "팀 생성/가입/조회/멤버 관리 API")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "팀 생성",
            description =
                    "새로운 팀(프로젝트 공간)을 생성합니다.\n\n" +
                            "동작:\n" +
                            "- 요청한 사용자가 팀의 기본 권한(예: OWNER)으로 자동 등록됩니다.\n" +
                            "- 팀 가입에 사용할 joinCode(초대코드)가 발급됩니다.\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeamCreateResponse.class),
                            examples = @ExampleObject(
                                    name = "팀 생성 성공",
                                    value = "{\n" +
                                            "  \"teamId\": 10,\n" +
                                            "  \"joinCode\": \"ABCD1234\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 오류(검증 실패/기간 형식 오류 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Bad Request\",\n" +
                                            "  \"message\": \"invalid request\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(로그인 필요)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Unauthorized\",\n" +
                                            "  \"message\": \"unauthorized\"\n" +
                                            "}"
                            )
                    )
            )
    })
    public ResponseEntity<TeamCreateResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        User me = meOrThrow();

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
    @Operation(
            summary = "팀 가입(초대코드)",
            description =
                    "초대코드(joinCode)를 이용해 팀에 가입합니다.\n\n" +
                            "주의:\n" +
                            "- 이미 가입한 팀이면 409(Conflict)를 반환합니다.\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 오류(초대코드 누락 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Bad Request\",\n" +
                                            "  \"message\": \"invalid request\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Unauthorized\",\n" +
                                            "  \"message\": \"unauthorized\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"message\": \"team not found\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 가입한 팀",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Conflict\",\n" +
                                            "  \"message\": \"already joined this team\"\n" +
                                            "}"
                            )
                    )
            )
    })
    public ResponseEntity<Void> joinTeam(@Valid @RequestBody TeamJoinRequest request) {
        User me = meOrThrow();
        teamService.joinTeam(me.getUserId(), request.getJoinCode());
        return ResponseEntity.ok().build();
    }

    // 3) 팀 멤버 목록 조회 (userId + username + role)
    @GetMapping("/{teamId}/members")
    @Operation(
            summary = "팀 멤버 목록 조회",
            description =
                    "팀에 속한 멤버 목록을 조회합니다.\n\n" +
                            "반환:\n" +
                            "- 각 멤버의 userId, username, role을 반환합니다.\n\n" +
                            "접근 조건:\n" +
                            "- 팀 멤버만 조회 가능(비멤버는 403)\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TeamMemberResponse.class)),
                            examples = @ExampleObject(
                                    value = "[\n" +
                                            "  { \"userId\": 1, \"username\": \"테스트유저\", \"role\": \"owner\" },\n" +
                                            "  { \"userId\": 2, \"username\": \"김멋사\", \"role\": \"member\" }\n" +
                                            "]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Unauthorized\",\n" +
                                            "  \"message\": \"unauthorized\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "팀 멤버가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Forbidden\",\n" +
                                            "  \"message\": \"not a team member\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"message\": \"team not found\"\n" +
                                            "}"
                            )
                    )
            )
    })
    public ResponseEntity<List<TeamMemberResponse>> getMembers(@PathVariable Long teamId) {
        User me = meOrThrow();

        teamService.getTeamOrThrow(teamId);              // 404
        teamService.getMember(teamId, me.getUserId());   // 403

        // repository도 username까지 조회하도록 변경된 메서드 사용
        List<Object[]> rows = teamMemberRepository.findUserIdUsernameAndRoleByTeamId(teamId);

        List<TeamMemberResponse> result = rows.stream()
                .map(r -> new TeamMemberResponse(
                        (Long) r[0],     // userId
                        (String) r[1],   // username
                        (String) r[2]    // role
                ))
                .toList();

        return ResponseEntity.ok(result);
    }


    // 4) 내 팀 목록 조회
    @GetMapping
    @Operation(
            summary = "내 팀 목록 조회",
            description =
                    "현재 사용자가 가입한 팀 목록을 조회합니다.\n\n" +
                            "특징:\n" +
                            "- 가입한 팀이 없으면 빈 배열([])을 반환합니다.\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MyTeamResponse.class)),
                            examples = @ExampleObject(
                                    value = "[\n" +
                                            "  {\n" +
                                            "    \"teamId\": 10,\n" +
                                            "    \"name\": \"Floorida 팀플\",\n" +
                                            "    \"startDate\": \"2026-01-01\",\n" +
                                            "    \"endDate\": \"2026-02-01\"\n" +
                                            "  }\n" +
                                            "]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Unauthorized\",\n" +
                                            "  \"message\": \"unauthorized\"\n" +
                                            "}"
                            )
                    )
            )
    })
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

    // 5) 팀 단건 조회 (팀 기본 정보 + myRole)
    @GetMapping("/{teamId}")
    @Operation(
            summary = "팀 단건 조회",
            description =
                    "팀의 기본 정보를 조회합니다.\n\n" +
                            "반환:\n" +
                            "- 팀 정보 + 현재 사용자의 myRole을 함께 반환합니다.\n\n" +
                            "접근 조건:\n" +
                            "- 팀 멤버만 조회 가능(비멤버 403)\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeamResponse.class),
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"teamId\": 10,\n" +
                                            "  \"name\": \"Floorida 팀플\",\n" +
                                            "  \"level\": 3,\n" +
                                            "  \"startDate\": \"2026-01-01\",\n" +
                                            "  \"endDate\": \"2026-02-01\",\n" +
                                            "  \"createdAt\": \"2026-01-01T10:00:00Z\",\n" +
                                            "  \"myRole\": \"OWNER\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Unauthorized\",\n" +
                                            "  \"message\": \"unauthorized\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "팀 멤버가 아님",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Forbidden\",\n" +
                                            "  \"message\": \"not a team member\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"error\": \"Not Found\",\n" +
                                            "  \"message\": \"team not found\"\n" +
                                            "}"
                            )
                    )
            )
    })
    public ResponseEntity<TeamResponse> getTeam(@PathVariable Long teamId) {
        User me = meOrThrow();

        Team team = teamService.getTeamOrThrow(teamId);
        TeamMember member = teamService.getMember(teamId, me.getUserId());

        return ResponseEntity.ok(new TeamResponse(
                team.getId(),
                team.getName(),
                team.getLevel(),
                team.getStartDate(),
                team.getEndDate(),
                team.getCreatedAt(),
                member.getRole()
        ));
    }

    // 6) 팀 삭제 (owner만 + 비밀번호 재확인)
    @DeleteMapping("/{teamId}")
    @Operation(
            summary = "팀 삭제",
            description =
                    "팀을 삭제합니다.\n\n" +
                            "접근 조건:\n" +
                            "- OWNER만 가능(권한 없으면 403)\n" +
                            "- 비밀번호 재입력 필요(불일치 시 400)\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공(응답 본문 없음)"),
            @ApiResponse(
                    responseCode = "400",
                    description = "비밀번호 불일치",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"message\": \"invalid password\" }"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"message\": \"unauthorized\" }"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"message\": \"no permission\" }"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"message\": \"team not found\" }"))
            )
    })
    public ResponseEntity<Void> deleteTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamDeleteRequest request
    ) {
        User me = meOrThrow();
        teamService.deleteTeamWithPassword(teamId, me.getUserId(), request.getPassword());
        return ResponseEntity.noContent().build();
    }


    // 7) 팀원 퇴출 (owner만)
    @DeleteMapping("/{teamId}/members/{targetUserId}")
    @Operation(
            summary = "팀원 퇴출",
            description =
                    "특정 팀원을 팀에서 퇴출합니다.\n\n" +
                            "접근 조건:\n" +
                            "- OWNER만 가능(권한 없으면 403)\n\n" +
                            "정책(409 Conflict):\n" +
                            "- owner 본인을 퇴출하려는 경우\n" +
                            "- owner를 퇴출하려는 경우\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "퇴출 성공(응답 본문 없음)"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Unauthorized\", \"message\": \"unauthorized\" }"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Forbidden\", \"message\": \"no permission\" }"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Not Found\", \"message\": \"team not found\" }"))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "정책 위반(OWNER 관련 퇴출 불가)",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "owner 본인 퇴출 불가",
                                            value = "{ \"error\": \"Conflict\", \"message\": \"owner cannot kick self\" }"),
                                    @ExampleObject(name = "owner 퇴출 불가",
                                            value = "{ \"error\": \"Conflict\", \"message\": \"cannot kick owner\" }")
                            })
            )
    })
    public ResponseEntity<Void> kickMember(@PathVariable Long teamId, @PathVariable Long targetUserId) {
        User me = meOrThrow();
        teamService.kickMember(teamId, me.getUserId(), targetUserId);
        return ResponseEntity.noContent().build();
    }

    // 8) 팀 탈퇴 (팀원 본인)
    @DeleteMapping("/{teamId}/leave")
    @Operation(
            summary = "팀 탈퇴",
            description =
                    "현재 사용자가 팀에서 탈퇴합니다.\n\n" +
                            "정책(409 Conflict):\n" +
                            "- OWNER는 탈퇴할 수 없고, 팀 삭제로 처리합니다.\n\n" +
                            "권한:\n" +
                            "- JWT 인증 필요"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공(응답 본문 없음)"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Unauthorized\", \"message\": \"unauthorized\" }"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "팀 멤버가 아님",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Forbidden\", \"message\": \"not a team member\" }"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팀을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Not Found\", \"message\": \"team not found\" }"))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "OWNER는 탈퇴 불가(팀 삭제 필요)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"error\": \"Conflict\", \"message\": \"owner cannot leave; delete team instead\" }"))
            )
    })
    public ResponseEntity<Void> leaveTeam(@PathVariable Long teamId) {
        User me = meOrThrow();
        teamService.leaveTeam(teamId, me.getUserId());
        return ResponseEntity.noContent().build();
    }


}
