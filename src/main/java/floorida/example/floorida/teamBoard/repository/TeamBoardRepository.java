package floorida.example.floorida.teamBoard.repository;


import floorida.example.floorida.teamBoard.entity.TeamBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBoardRepository extends JpaRepository<TeamBoard, Long> {

    // 📌 팀 게시판 목록 (댓글 수 포함)
    @Query("""
        SELECT 
            b.id AS id,
            b.content AS content,
            b.likeCount AS likeCount,
            b.createdAt AS createdAt,
            u.userId AS userId,
            u.username AS username,
            COUNT(c.id) AS commentCount
        FROM TeamBoard b
        JOIN b.user u
        LEFT JOIN Comment c ON c.board.id = b.id
        WHERE b.team.id = :teamId
        GROUP BY b.id, u.userId, u.username
        ORDER BY b.createdAt DESC
    """)
    List<TeamBoardListView> findBoardListByTeamId(@Param("teamId") Long teamId);

    // 📌 게시글 단건 조회 (팀 검증)
    Optional<TeamBoard> findByIdAndTeamId(Long boardId, Long teamId);

    // 📌 게시글 삭제 (팀 검증)
    void deleteByIdAndTeamId(Long boardId, Long teamId);
}