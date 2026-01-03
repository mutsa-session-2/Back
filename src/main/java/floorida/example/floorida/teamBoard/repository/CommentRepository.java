package floorida.example.floorida.teamBoard.repository;

import floorida.example.floorida.teamBoard.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);

    Optional<Comment> findByIdAndBoardId(Long commentId, Long boardId);

    long countByBoardId(Long boardId);
}