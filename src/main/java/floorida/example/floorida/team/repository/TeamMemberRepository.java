package floorida.example.floorida.team.repository;

import floorida.example.floorida.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeam_IdAndUser_UserId(Long teamId, Long userId);

    Optional<TeamMember> findByTeam_IdAndUser_UserId(Long teamId, Long userId);

    List<TeamMember> findByTeam_Id(Long teamId);

    // 내가 속한 팀 id 목록
    @Query("select tm.team.id from TeamMember tm where tm.user.userId = :userId")
    List<Long> findTeamIdsByUserId(@Param("userId") Long userId);

    // 특정 팀 멤버 조회: userId + role만 가볍게
    @Query("""
           select tm.user.userId, tm.role
           from TeamMember tm
           where tm.team.id = :teamId
           """)
    List<Object[]> findUserIdAndRoleByTeamId(@Param("teamId") Long teamId);

    void deleteByTeam_Id(Long teamId);


}


