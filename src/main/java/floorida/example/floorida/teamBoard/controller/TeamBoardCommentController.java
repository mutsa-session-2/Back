package floorida.example.floorida.teamBoard.controller;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import floorida.example.floorida.teamBoard.dto.request.TeamBoardCommentCreateRequest;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCommentCreateResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCommentDetailResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCommentListResponse;
import floorida.example.floorida.teamBoard.service.TeamBoardCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/boards/{boardId}/comments")
@Tag(name = "Team Board Comment", description = "팀 게시판 댓글 관련 API")
public class TeamBoardCommentController {

    private final TeamBoardCommentService commentService;

    /**
     * 📌 댓글 전체 조회
     */
    @GetMapping
    @Operation(
            summary = "댓글 목록 조회",
            description = "특정 게시글에 달린 모든 댓글을 조회합니다."
    )
    public ResponseEntity<List<TeamBoardCommentListResponse>> getComments(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,

            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long boardId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TeamBoardCommentListResponse> responses =
                commentService.getComments(
                        teamId,
                        boardId,
                        userDetails.getUserId()
                );

        return ResponseEntity.ok(responses);
    }

    /**
     * 📌 특정 댓글 조회
     */
    @GetMapping("/{commentId}")
    @Operation(
            summary = "댓글 단건 조회",
            description = "특정 게시글의 댓글 하나를 조회합니다."
    )
    public ResponseEntity<TeamBoardCommentDetailResponse> getComment(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,

            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long boardId,

            @Parameter(description = "댓글 ID", example = "5")
            @PathVariable Long commentId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TeamBoardCommentDetailResponse response =
                commentService.getComment(
                        teamId,
                        boardId,
                        commentId,
                        userDetails.getUserId()
                );

        return ResponseEntity.ok(response);
    }

    /**
     * 📌 댓글 등록
     */
    @PostMapping
    @Operation(
            summary = "댓글 작성",
            description = "특정 게시글에 댓글을 작성합니다."
    )
    public ResponseEntity<TeamBoardCommentCreateResponse> createComment(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,

            @Parameter(description = "게시글 ID", example = "10")
            @PathVariable Long boardId,

            @Parameter(description = "댓글 작성 요청 DTO")
            @RequestBody @Valid TeamBoardCommentCreateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TeamBoardCommentCreateResponse response =
                commentService.createComment(
                        teamId,
                        boardId,
                        userDetails.getUserId(),
                        request
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/teams/" + teamId +
                                        "/boards/" + boardId +
                                        "/comments/" + response.getCommentId()
                        )
                )
                .body(response);
    }
}
