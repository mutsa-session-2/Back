package floorida.example.floorida.Item.repository;

import floorida.example.floorida.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberCharacterRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeam_IdAndUser_UserId(Long teamId, Long userId);

    List<TeamMember> findAllByTeam_Id(Long teamId);
}