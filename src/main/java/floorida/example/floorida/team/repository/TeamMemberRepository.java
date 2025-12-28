package floorida.example.floorida.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember,Long>{

    boolean existsByTeam_IdAndUser_Id(Long teamId, Long userId);

    Optional<TeamMember> findByTeam_IdAndUser_Id(Long teamId, Long userId);

    List<TeamMember> findByTeam_Id(Long teamId);
}
