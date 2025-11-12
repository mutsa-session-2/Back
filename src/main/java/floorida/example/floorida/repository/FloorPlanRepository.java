package floorida.example.floorida.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import floorida.example.floorida.entity.FloorPlan;

@Repository
public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long> {
    List<FloorPlan> findBySchedule_ScheduleId(Long scheduleId);
    List<FloorPlan> findByScheduledDate(LocalDate date);
    List<FloorPlan> findBySchedule_ScheduleIdAndScheduledDate(Long scheduleId, LocalDate date);
    
    // 특정 사용자의 특정 날짜 할 일 조회
    List<FloorPlan> findByCreatorUserIdAndScheduledDate(Long creatorUserId, LocalDate date);
}
