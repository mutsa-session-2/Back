package floorida.example.floorida.teamBoard.repository;

import floorida.example.floorida.teamBoard.entity.TeamBoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamBoardLikeRepository
        extends JpaRepository<TeamBoardLike, Long> {

    Optional<TeamBoardLike> findByBoardIdAndUser_UserId(Long boardId, Long userId);

    boolean existsByBoardIdAndUser_UserId(Long boardId, Long userId);

    long countByBoardId(Long boardId);
}