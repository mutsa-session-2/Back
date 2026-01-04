package floorida.example.floorida.team.controller;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.service.CurrentUserService;
import floorida.example.floorida.team.dto.*;
import floorida.example.floorida.team.service.TeamFloorService;
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
@Tag(name = "팀 할 일", description = "팀 스페이스 할 일(TeamFloor) 생성/조회/배정/완료 API")
@SecurityRequirement(name = "bearerAuth")
public class TeamFloorController {

    private final TeamFloorService teamFloorService;
    private final CurrentUserService currentUserService;

    private User meOrThrow() {
        return currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("unauthorized"));
    }

    // 1) 생성 (admin/owner)
    @PostMapping("/{teamId}/floors")
    @Operation(
            summary = "팀 할 일 생성",
            description =
                    "팀 스페이스에 할 일을 생성합니다.\n" +
                            "- assigneeUserIds는 비워도 가능(미정 생성)\n" +
                            "- dueDate는 팀 프로젝트 기간(startDate~endDate) 안에서만 허용"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Long.class),
                            examples = @ExampleObject(value = "123"))),
            @ApiResponse(responseCode = "400", description = "요청값 오류(dueDate 범위 위반 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(OWNER/ADMIN 아님)"),
            @ApiResponse(responseCode = "404", description = "팀/사용자 등을 찾을 수 없음")
    })
    public ResponseEntity<Long> createTeamFloor(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamFloorCreateRequest request
    ) {
        User me = meOrThrow();
        Long floorId = teamFloorService.createTeamFloor(me.getUserId(), teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(floorId);
    }

    // 2) 팀 할 일 목록 조회 (member)
    // - 배정자 없는 할 일은 assigneeUserIds == [] (미정)
    @GetMapping("/{teamId}/floors")
    @Operation(summary = "팀 할 일 목록 조회", description = "팀 멤버가 팀 할 일 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TeamFloorResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "팀 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    })
    public ResponseEntity<List<TeamFloorResponse>> listTeamFloors(@PathVariable Long teamId) {
        User me = meOrThrow();
        return ResponseEntity.ok(teamFloorService.listTeamFloors(me.getUserId(), teamId));
    }

    // 3) 할 일 상세 조회 (member)
    @GetMapping("/{teamId}/floors/{teamFloorId}")
    @Operation(summary = "팀 할 일 상세 조회", description = "팀 멤버가 특정 할 일을 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TeamFloorDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "팀 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음")
    })
    public ResponseEntity<TeamFloorDetailResponse> getDetail(
            @PathVariable Long teamId,
            @PathVariable Long teamFloorId
    ) {
        User me = meOrThrow();
        return ResponseEntity.ok(teamFloorService.getTeamFloorDetail(me.getUserId(), teamId, teamFloorId));
    }

    // 4) 배정자 변경(재배정) (admin/owner) - 빈 배열 허용(미정으로 만들기 가능)
    @PatchMapping("/floors/{teamFloorId}/assignees")
    @Operation(
            summary = "팀 할 일 배정자 변경(재배정)",
            description =
                    "특정 할 일의 배정자 목록을 교체합니다.\n" +
                            "- 빈 배열/NULL도 허용(미정으로 변경)\n" +
                            "- OWNER/ADMIN만 가능"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(OWNER/ADMIN 아님)"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음")
    })
    public ResponseEntity<Void> updateAssignees(
            @PathVariable Long teamFloorId,
            @RequestBody TeamFloorAssigneesUpdateRequest request
    ) {
        User me = meOrThrow();
        teamFloorService.updateAssignees(me.getUserId(), teamFloorId, request);
        return ResponseEntity.ok().build();
    }

    // 5) 완료 토글 ON (assignee OR admin/owner)
    @PostMapping("/floors/{teamFloorId}/complete")
    @Operation(
            summary = "팀 할 일 완료(토글 ON)",
            description =
                    "배정자 또는 OWNER/ADMIN이 완료 처리할 수 있습니다.\n" +
                            "- 이미 완료된 경우 실패가 아니라 alreadyCompleted=true\n" +
                            "- 완료 전환(false->true) 성공 시 팀 레벨 +1"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TeamFloorCompleteResponse.class),
                            examples = @ExampleObject(value = "{ \"alreadyCompleted\": false, \"levelUp\": true }"))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(배정자도 아니고 OWNER/ADMIN도 아님)"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음")
    })
    public ResponseEntity<TeamFloorCompleteResponse> complete(@PathVariable Long teamFloorId) {
        User me = meOrThrow();
        TeamFloorService.CompleteResult r = teamFloorService.complete(me.getUserId(), teamFloorId);
        return ResponseEntity.ok(new TeamFloorCompleteResponse(r.isAlreadyCompleted(), r.isLevelUp()));
    }

    // 6) 완료 취소 토글 OFF (assignee OR admin/owner)
    @PostMapping("/floors/{teamFloorId}/cancel")
    @Operation(
            summary = "팀 할 일 완료 취소(토글 OFF)",
            description =
                    "배정자 또는 OWNER/ADMIN이 완료 취소할 수 있습니다.\n" +
                            "- 이미 미완료인 경우 실패가 아니라 alreadyIncomplete=true\n" +
                            "- 취소 전환(true->false) 성공 시 팀 레벨 -1"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TeamFloorCancelResponse.class),
                            examples = @ExampleObject(value = "{ \"alreadyIncomplete\": false, \"levelDown\": true }"))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음(배정자도 아니고 OWNER/ADMIN도 아님)"),
            @ApiResponse(responseCode = "404", description = "할 일을 찾을 수 없음")
    })
    public ResponseEntity<TeamFloorCancelResponse> cancel(@PathVariable Long teamFloorId) {
        User me = meOrThrow();
        TeamFloorService.CancelResult r = teamFloorService.cancel(me.getUserId(), teamFloorId);
        return ResponseEntity.ok(new TeamFloorCancelResponse(r.isAlreadyIncomplete(), r.isLevelDown()));
    }
}
