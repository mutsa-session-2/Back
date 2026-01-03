package floorida.example.floorida.teamBoard.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.repository.TeamRepository;
import floorida.example.floorida.teamBoard.dto.request.TeamBoardCreateRequest;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCreateResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardDetailResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardListResponse;
import floorida.example.floorida.teamBoard.entity.TeamBoard;
import floorida.example.floorida.teamBoard.repository.TeamBoardListView;
import floorida.example.floorida.teamBoard.repository.TeamBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamBoardService {

    private final TeamBoardRepository teamBoardRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    /**
     * 📌 팀 게시판 목록 조회
     */
    public List<TeamBoardListResponse> getTeamBoardList(Long teamId) {

        List<TeamBoardListView> views =
                teamBoardRepository.findBoardListByTeamId(teamId);

        return views.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Projection → DTO 변환
     */
    private TeamBoardListResponse toResponse(TeamBoardListView view) {
        return new TeamBoardListResponse(
                view.getId(),
                view.getContent(),
                view.getUserId(),
                view.getUsername(),
                view.getLikeCount(),
                view.getCommentCount(),
                view.getCreatedAt()
        );
    }

    @Transactional
    public TeamBoardCreateResponse createBoard(
            Long teamId,
            Long userId,
            TeamBoardCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        validateTeamMember(team, userId);
        TeamBoard board = new TeamBoard(
                team,
                user,
                request.getContent()
        );

        TeamBoard saved = teamBoardRepository.save(board);

        return new TeamBoardCreateResponse(
                saved.getId(),
                saved.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
        );
    }
    //단건 조회
    @Transactional(readOnly = true)
    public TeamBoardDetailResponse getBoardDetail(
            Long teamId,
            Long boardId,
            Long userId
    ) {
        // 1️⃣ 팀 + 유저 검증
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        validateTeamMember(team, userId);

        // 2️⃣ 게시글 조회 (teamId + boardId)
        TeamBoard board = teamBoardRepository
                .findByIdAndTeamId(boardId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 3️⃣ Entity → DTO
        return TeamBoardDetailResponse.builder()
                .boardId(board.getId())
                .teamId(teamId)
                .content(board.getContent())
                .writerId(board.getUser().getUserId())
                .writerName(board.getUser().getUsername())
                .createdAt(board.getCreatedAt())
                .build();
    }

    private void validateTeamMember(Team team, Long userId) {
        boolean isMember = team.getTeamMembers().stream()
                .anyMatch(tm ->
                        tm.getUser().getUserId().equals(userId)
                );

        if (!isMember) {
            throw new IllegalArgumentException("팀 멤버가 아닙니다.");
        }
    }

}