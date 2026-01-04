package floorida.example.floorida.team.repository;

import floorida.example.floorida.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);

    List<Team> findByIdIn(List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Team t set t.level = t.level + 1 where t.id = :teamId")
    int incrementLevel(@Param("teamId") Long teamId);

    // level이 1 아래로 내려가면 안 되면 조건 추가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Team t set t.level = t.level - 1 where t.id = :teamId and t.level > 1")
    int decrementLevel(@Param("teamId") Long teamId);
}
