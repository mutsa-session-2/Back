package floorida.example.floorida.teamBoard.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.teamBoard.entity.TeamBoard;
import floorida.example.floorida.teamBoard.entity.TeamBoardLike;
import floorida.example.floorida.teamBoard.repository.CommentRepository;
import floorida.example.floorida.teamBoard.repository.TeamBoardLikeRepository;
import floorida.example.floorida.teamBoard.repository.TeamBoardRepository;
import floorida.example.floorida.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamBoardLikeService {

    private final TeamBoardLikeRepository likeRepository;
    private final TeamBoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public boolean toggleLike(Long boardId, Long userId) {

        TeamBoard board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        return likeRepository.findByBoardIdAndUser_UserId(boardId, userId)
                .map(existingLike -> {
                    // 👍 이미 좋아요 → 취소
                    likeRepository.delete(existingLike);
                    board.decreaseLike();
                    return false;
                })
                .orElseGet(() -> {
                    // 👍 좋아요 추가
                    likeRepository.save(new TeamBoardLike(board, user));
                    board.increaseLike();
                    return true;
                });
    }
}
