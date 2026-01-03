package floorida.example.floorida.teamBoard.controller;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import floorida.example.floorida.teamBoard.dto.request.TeamBoardCreateRequest;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCreateResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardDetailResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardListResponse;
import floorida.example.floorida.teamBoard.service.TeamBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

// Swagger/OpenAPI 어노테이션
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/boards")
@Tag(name = "Team Board", description = "팀 게시판 관련 API")
public class TeamBoardController{

    private final TeamBoardService teamBoardService;

    /**
     * 📌 팀 게시판 목록 조회
     */
    @GetMapping
    @Operation(summary = "팀 게시판 목록 조회", description = "팀의 모든 게시글 목록을 조회합니다.")
    public ResponseEntity<List<TeamBoardListResponse>> getBoardList(
            @Parameter(description = "조회할 팀 ID", example = "1")
            @PathVariable Long teamId
    ) {
        List<TeamBoardListResponse> boards =
                teamBoardService.getTeamBoardList(teamId);

        return ResponseEntity.ok(boards);
    }

    /**
     * 📌 게시글 작성
     */
    @PostMapping
    @Operation(summary = "게시글 작성", description = "팀 게시판에 새로운 게시글을 작성합니다.")
    public ResponseEntity<TeamBoardCreateResponse> createBoard(
            @Parameter(description = "게시글 작성할 팀 ID", example = "1")
            @PathVariable Long teamId,
            @Parameter(description = "게시글 작성 요청 DTO")
            @RequestBody @Valid TeamBoardCreateRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TeamBoardCreateResponse response =
                teamBoardService.createBoard(
                        teamId,
                        userDetails.getUserId(),
                        request
                );

        return ResponseEntity
                .created(URI.create("/teams/" + teamId + "/boards/" + response.getBoardId()))
                .body(response);
    }

    /**
     * 📌 게시글 단건 조회
     */
    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 단건 조회", description = "팀 게시판의 특정 게시글을 조회합니다.")
    public ResponseEntity<TeamBoardDetailResponse> getBoardDetail(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,

            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long boardId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TeamBoardDetailResponse response =
                teamBoardService.getBoardDetail(
                        teamId,
                        boardId,
                        userDetails.getUserId()
                );

        return ResponseEntity.ok(response);
    }
}
