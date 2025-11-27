package floorida.example.floorida.service;

import floorida.example.floorida.dto.DailyCompletionResponse;
import floorida.example.floorida.dto.FloorStatusResponse;
import floorida.example.floorida.dto.MonthlyScheduleResponse;
import floorida.example.floorida.entity.FloorPlan;
import floorida.example.floorida.entity.FloorStatus;
import floorida.example.floorida.entity.Schedule;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.FloorPlanRepository;
import floorida.example.floorida.repository.FloorStatusRepository;
import floorida.example.floorida.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FloorStatusService {
    
    private final FloorStatusRepository floorStatusRepository;
    private final FloorPlanRepository floorPlanRepository;
    private final CurrentUserService currentUserService;

    private final ScheduleRepository scheduleRepository;

    public List<FloorStatusResponse> getStatusByDate(LocalDate date) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        List<FloorPlan> floors = floorPlanRepository.findAllByCreatorUserIdAndScheduledDate(user.getUserId(), date);
        List<FloorStatus> statuses = floorStatusRepository.findAllByUser_UserIdAndFloor_ScheduledDate(user.getUserId(), date);
        
        Map<Long, FloorStatus> statusMap = statuses.stream()
                .collect(Collectors.toMap(s -> s.getFloor().getFloorId(), s -> s));
        
        return floors.stream()
                .map(f -> FloorStatusResponse.builder()
                        .floorId(f.getFloorId())
                        .scheduleId(f.getSchedule().getScheduleId())
                        .scheduleTitle(f.getSchedule().getTitle())
                        .scheduleColor(f.getSchedule().getColor())
                        .floorTitle(f.getTitle())
                        .scheduledDate(f.getScheduledDate())
                        .completed(statusMap.containsKey(f.getFloorId()) && statusMap.get(f.getFloorId()).getIsCompleted())
                        .build())
                .collect(Collectors.toList());
    }

    public List<DailyCompletionResponse> getCalendarData(LocalDate start, LocalDate end) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        List<FloorPlan> floors = floorPlanRepository.findAllByCreatorUserIdAndScheduledDateBetween(user.getUserId(), start, end);
        List<FloorStatus> statuses = floorStatusRepository.findAllByUser_UserIdAndFloor_ScheduledDateBetween(user.getUserId(), start, end);
        
        Map<Long, FloorStatus> statusMap = statuses.stream()
                .collect(Collectors.toMap(s -> s.getFloor().getFloorId(), s -> s));
        
        // 날짜별로 그룹핑
        Map<LocalDate, List<FloorPlan>> floorsByDate = floors.stream()
                .filter(f -> f.getScheduledDate() != null)
                .collect(Collectors.groupingBy(FloorPlan::getScheduledDate));
        
        List<DailyCompletionResponse> result = new ArrayList<>();
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<FloorPlan> dayFloors = floorsByDate.getOrDefault(date, List.of());
            
            if (dayFloors.isEmpty()) {
                continue; // 해당 날짜에 Floor 없으면 스킵
            }
            
            List<FloorStatusResponse> floorResponses = dayFloors.stream()
                    .map(f -> FloorStatusResponse.builder()
                            .floorId(f.getFloorId())
                            .scheduleId(f.getSchedule().getScheduleId())
                            .scheduleTitle(f.getSchedule().getTitle())
                            .scheduleColor(f.getSchedule().getColor())
                            .floorTitle(f.getTitle())
                            .scheduledDate(f.getScheduledDate())
                            .completed(statusMap.containsKey(f.getFloorId()) && statusMap.get(f.getFloorId()).getIsCompleted())
                            .build())
                    .collect(Collectors.toList());
            
            long completedCount = floorResponses.stream().filter(FloorStatusResponse::isCompleted).count();
            int totalCount = floorResponses.size();
            int rate = totalCount > 0 ? (int) ((completedCount * 100) / totalCount) : 0;
            
            result.add(DailyCompletionResponse.builder()
                    .date(date)
                    .totalFloors(totalCount)
                    .completedFloors((int) completedCount)
                    .completionRate(rate)
                    .floors(floorResponses)
                    .build());
        }
        
        return result;
    }

    public List<MonthlyScheduleResponse> getMonthlyScheduleData(LocalDate start, LocalDate end) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        // 해당 기간에 걸쳐있는 모든 스케줄 조회
        // startDate <= end AND endDate >= start
        List<Schedule> schedules = scheduleRepository.findByCreatorUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getUserId(), end, start);
        
        return schedules.stream()
                .map(s -> MonthlyScheduleResponse.builder()
                        .scheduleId(s.getScheduleId())
                        .title(s.getTitle())
                        .color(s.getColor())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }
}
