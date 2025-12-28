package floorida.example.floorida.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import floorida.example.floorida.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team,Long>{
    Optional<Team> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
}
