package floorida.example.floorida.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import floorida.example.floorida.team.entity.TeamMember;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeam_IdAndUser_UserId(Long teamId, Long userId);

    Optional<TeamMember> findByTeam_IdAndUser_UserId(Long teamId, Long userId);

    List<TeamMember> findByTeam_Id(Long teamId);
}

