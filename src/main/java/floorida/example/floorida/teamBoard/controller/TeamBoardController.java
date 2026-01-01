package floorida.example.floorida.teamBoard.controller;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import floorida.example.floorida.teamBoard.dto.request.TeamBoardCreateRequest;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCreateResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardListResponse;
import floorida.example.floorida.teamBoard.service.TeamBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/boards")
public class TeamBoardController{

    private final TeamBoardService teamBoardService;

    /**
     * 📌 팀 게시판 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<TeamBoardListResponse>> getBoardList(
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
    public ResponseEntity<TeamBoardCreateResponse> createBoard(
            @PathVariable Long teamId,
            @RequestBody @Valid TeamBoardCreateRequest request,
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
}
