package floorida.example.floorida.team.repository;

import floorida.example.floorida.team.entity.TeamFloor;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TeamFloorRepository extends JpaRepository<TeamFloor, Long> {

    // 팀의 할 일 목록
    List<TeamFloor> findByTeam_IdOrderByDueDateAscCreatedAtAsc(Long teamId);

    // 특정 팀에 속한 할 일인지까지 같이 확인할 때 유용
    Optional<TeamFloor> findByIdAndTeam_Id(Long floorId, Long teamId);

    // 미정(배정자 없는) 할 일 목록
    @Query("""
        select f
          from TeamFloor f
         where f.team.id = :teamId
           and not exists (
               select 1
                 from TeamFloorStatus s
                where s.teamFloor.id = f.id
           )
         order by f.dueDate asc, f.createdAt asc
    """)
    List<TeamFloor> findUnassignedFloors(@Param("teamId") Long teamId);

    // 전체 완료 처리(처음 완료일 때만)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamFloor f
           set f.completed = true,
               f.completedAt = :now
         where f.id = :floorId
           and f.completed = false
    """)
    int markCompletedIfNotCompleted(@Param("floorId") Long floorId, @Param("now") Instant now);

    // 전체 취소 처리(완료 상태일 때만)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TeamFloor f
           set f.completed = false,
               f.completedAt = null
         where f.id = :floorId
           and f.completed = true
    """)
    int cancelIfCompleted(@Param("floorId") Long floorId);

    // 팀 삭제 시 사용
    void deleteByTeam_Id(Long teamId);
}

