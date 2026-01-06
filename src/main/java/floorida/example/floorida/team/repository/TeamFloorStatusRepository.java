package floorida.example.floorida.team.repository;

import floorida.example.floorida.team.entity.TeamFloorStatus;
import floorida.example.floorida.team.entity.TeamFloorStatusId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TeamFloorStatusRepository extends JpaRepository<TeamFloorStatus, TeamFloorStatusId> {

    List<TeamFloorStatus> findByIdTeamFloorIdIn(List<Long> teamFloorIds);
    // 배정자(담당자)인지 체크
    boolean existsByIdTeamFloorIdAndIdUserId(Long teamFloorId, Long userId);

    // 특정 할 일의 배정자 목록(필요하면 사용)
    List<TeamFloorStatus> findByIdTeamFloorId(Long teamFloorId);

    // 배정자 전체 교체 시 유용
    void deleteByIdTeamFloorId(Long teamFloorId);

    // 탈퇴/퇴출 시 해당 팀에서 배정 제거
    void deleteByIdUserIdAndTeamFloor_Team_Id(Long userId, Long teamId);

    // 팀 폭파 시 배정 제거
    void deleteByTeamFloor_Team_Id(Long teamId);

    // (선택) 개인 완료 기록
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamFloorStatus s
           set s.completed = true,
               s.completedAt = :now
         where s.id.teamFloorId = :floorId
           and s.id.userId = :userId
           and s.completed = false
    """)
    int markAssigneeCompletedIfNotCompleted(
            @Param("floorId") Long floorId,
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    // 취소 시 개인 완료 기록 전체 리셋
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamFloorStatus s
           set s.completed = false,
               s.completedAt = null
         where s.id.teamFloorId = :floorId
    """)
    int resetAllAssigneesStatus(@Param("floorId") Long floorId);
}
