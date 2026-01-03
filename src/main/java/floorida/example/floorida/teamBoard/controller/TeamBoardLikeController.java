package floorida.example.floorida.teamBoard.controller;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import floorida.example.floorida.teamBoard.service.TeamBoardLikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/boards/{boardId}/likes")
public class TeamBoardLikeController {

    private final TeamBoardLikeService likeService;

    @PostMapping
    @Operation(summary = "게시글 좋아요 토글")
    public boolean toggleLike(
            @PathVariable Long teamId,   // 구조 통일용 (로직에는 사용 안 해도 됨)
            @PathVariable Long boardId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return likeService.toggleLike(boardId, userDetails.getUserId());
    }
}