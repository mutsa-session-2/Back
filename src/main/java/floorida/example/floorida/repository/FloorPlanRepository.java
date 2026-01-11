package floorida.example.floorida.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import floorida.example.floorida.entity.FloorPlan;

@Repository
public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long> {
    List<FloorPlan> findBySchedule_ScheduleId(Long scheduleId);
    List<FloorPlan> findByScheduledDate(LocalDate date);
    List<FloorPlan> findBySchedule_ScheduleIdAndScheduledDate(Long scheduleId, LocalDate date);
    
    // 특정 사용자의 특정 날짜 할 일 조회
    List<FloorPlan> findByCreatorUserIdAndScheduledDate(Long creatorUserId, LocalDate date);
    
    // 특정 사용자의 모든 Floor 조회 (날짜 있는 것만)
    List<FloorPlan> findAllByCreatorUserIdAndScheduledDate(Long creatorUserId, LocalDate date);
    
    // 특정 사용자의 기간별 Floor 조회
    List<FloorPlan> findAllByCreatorUserIdAndScheduledDateBetween(Long creatorUserId, LocalDate start, LocalDate end);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                delete from FloorPlan fp
                where fp.schedule.scheduleId = :scheduleId
                    and fp.creatorUserId = :userId
                """)
        int deleteByScheduleIdAndCreatorUserId(
                        @Param("scheduleId") Long scheduleId,
                        @Param("userId") Long userId
        );

        /**
         * 개인 플레이스용: 오늘 이전 날짜의 미달성 Floor 목록을 조회합니다.
         * - 미달성 기준: scheduledDate < today AND (완료 상태(FloorStatus, isCompleted=true) 없음)
         * - schedule은 응답 DTO를 만들기 위해 fetch join 합니다.
         */
        @Query("""
                select fp
                from FloorPlan fp
                join fetch fp.schedule s
                where fp.creatorUserId = :userId
                    and s.teamId is null
                    and fp.scheduledDate is not null
                    and fp.scheduledDate < :today
                    and not exists (
                            select 1
                            from FloorStatus fs
                            where fs.floor = fp
                                and fs.user.userId = :userId
                                and fs.isCompleted = true
                    )
                order by fp.scheduledDate asc
                """)
        List<FloorPlan> findUncompletedFloorsBeforeDate(
                        @Param("userId") Long userId,
                        @Param("today") LocalDate today
        );
}
