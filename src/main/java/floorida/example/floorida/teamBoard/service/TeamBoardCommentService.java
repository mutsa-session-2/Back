package floorida.example.floorida.teamBoard.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.repository.TeamRepository;
import floorida.example.floorida.teamBoard.dto.request.TeamBoardCommentCreateRequest;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCommentCreateResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCommentDetailResponse;
import floorida.example.floorida.teamBoard.dto.response.TeamBoardCommentListResponse;
import floorida.example.floorida.teamBoard.entity.Comment;
import floorida.example.floorida.teamBoard.entity.TeamBoard;
import floorida.example.floorida.teamBoard.repository.CommentRepository;
import floorida.example.floorida.teamBoard.repository.TeamBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamBoardCommentService {

    private final CommentRepository commentRepository;
    private final TeamBoardRepository teamBoardRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    /**
     * 📌 댓글 목록 조회
     */
    public List<TeamBoardCommentListResponse> getComments(
            Long teamId,
            Long boardId,
            Long userId
    ) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        validateTeamMember(team, userId);

        TeamBoard board = teamBoardRepository.findByIdAndTeamId(boardId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        return commentRepository.findByBoardIdOrderByCreatedAtAsc(board.getId())
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    /**
     * 📌 댓글 단건 조회
     */
    public TeamBoardCommentDetailResponse getComment(
            Long teamId,
            Long boardId,
            Long commentId,
            Long userId
    ) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        validateTeamMember(team, userId);

        TeamBoard board = teamBoardRepository.findByIdAndTeamId(boardId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Comment comment = commentRepository
                .findByIdAndBoardId(commentId, board.getId())
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        return toDetailResponse(comment);
    }

    /**
     * 📌 댓글 작성
     */
    @Transactional
    public TeamBoardCommentCreateResponse createComment(
            Long teamId,
            Long boardId,
            Long userId,
            TeamBoardCommentCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        validateTeamMember(team, userId);

        TeamBoard board = teamBoardRepository.findByIdAndTeamId(boardId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Comment comment = new Comment(board, user, request.getContent());
        Comment saved = commentRepository.save(comment);

        return new TeamBoardCommentCreateResponse(
                saved.getId(),
                saved.getCreatedAt()
        );
    }

    /**
     * 팀 멤버 검증
     */
    private void validateTeamMember(Team team, Long userId) {
        boolean isMember = team.getTeamMembers().stream()
                .anyMatch(tm -> tm.getUser().getUserId().equals(userId));

        if (!isMember) {
            throw new IllegalArgumentException("팀 멤버가 아닙니다.");
        }
    }

    /**
     * Entity → 목록 DTO
     */
    private TeamBoardCommentListResponse toListResponse(Comment comment) {
        return new TeamBoardCommentListResponse(
                comment.getId(),
                comment.getUser().getUserId(),
                comment.getUser().getUsername(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    /**
     * Entity → 단건 DTO
     */
    private TeamBoardCommentDetailResponse toDetailResponse(Comment comment) {
        return TeamBoardCommentDetailResponse.builder()
                .commentId(comment.getId())
                .boardId(comment.getBoard().getId())
                .writerId(comment.getUser().getUserId())
                .writerName(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
