package floorida.example.floorida.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.FloorResponse;
import floorida.example.floorida.entity.FloorPlan;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.FloorPlanRepository;

@Service
public class FloorService {
    private final FloorPlanRepository floorPlanRepository;
    private final CurrentUserService currentUserService;

    public FloorService(FloorPlanRepository floorPlanRepository, CurrentUserService currentUserService) {
        this.floorPlanRepository = floorPlanRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<FloorResponse> getTodayFloors() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        LocalDate today = LocalDate.now();
        List<FloorPlan> floors = floorPlanRepository.findByCreatorUserIdAndScheduledDate(user.getUserId(), today);
        
        return floors.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FloorResponse> getFloorsByDate(LocalDate date) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        List<FloorPlan> floors = floorPlanRepository.findByCreatorUserIdAndScheduledDate(user.getUserId(), date);
        
        return floors.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FloorResponse toResponse(FloorPlan floor) {
        return FloorResponse.builder()
                .floorId(floor.getFloorId())
                .scheduleId(floor.getSchedule().getScheduleId())
                .scheduleTitle(floor.getSchedule().getTitle())
                .scheduleColor(floor.getSchedule().getColor())
                .floorTitle(floor.getTitle())
                .scheduledDate(floor.getScheduledDate())
                .build();
    }
}
