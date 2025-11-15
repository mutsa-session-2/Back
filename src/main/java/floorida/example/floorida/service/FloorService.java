package floorida.example.floorida.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.FloorResponse;
import floorida.example.floorida.entity.FloorPlan;
import floorida.example.floorida.entity.FloorStatus;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.FloorPlanRepository;
import floorida.example.floorida.repository.FloorStatusRepository;

@Service
public class FloorService {
    private final FloorPlanRepository floorPlanRepository;
    private final FloorStatusRepository floorStatusRepository;
    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;

    public FloorService(FloorPlanRepository floorPlanRepository,
                        FloorStatusRepository floorStatusRepository,
                        CurrentUserService currentUserService,
                        UserProfileService userProfileService) {
        this.floorPlanRepository = floorPlanRepository;
        this.floorStatusRepository = floorStatusRepository;
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
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

    /**
     * Floor 완료 처리 (퀘스트 체크) - 10코인 지급
     */
    @Transactional
    public void completeFloor(Long floorId) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        FloorPlan floor = floorPlanRepository.findById(floorId)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found"));

        // 본인이 생성한 Floor인지 확인
        if (!floor.getCreatorUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Not authorized to complete this floor");
        }

        // 이미 완료된 Floor인지 확인 (중복 코인 방지)
        if (floorStatusRepository.existsByFloor_FloorIdAndUser_UserId(floorId, user.getUserId())) {
            throw new IllegalArgumentException("Floor already completed");
        }

        // 완료 상태 저장
        FloorStatus status = new FloorStatus();
        status.setFloor(floor);
        status.setUser(user);
        status.setIsCompleted(true);
        status.setCompletedAt(Instant.now());
        floorStatusRepository.save(status);

        // 10코인 지급
        userProfileService.addPoints(user.getUserId(), 10);

        // 개인 층수 +1 (오늘 할 일 하나 완료할 때마다 한 층 올라감)
        userProfileService.incrementPersonalLevel(user.getUserId());
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
