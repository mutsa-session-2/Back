package floorida.example.floorida.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.AiScheduleRequest;
import floorida.example.floorida.dto.ScheduleCreateRequest;
import floorida.example.floorida.dto.ScheduleResponse;
import floorida.example.floorida.entity.FloorPlan;
import floorida.example.floorida.entity.Schedule;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.ScheduleRepository;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final CurrentUserService currentUserService;
    private final AiPlanningService aiPlanningService;

    // 예쁜 팔레트에서 자동 배정 (사용자가 color 미입력 시 사용)
    private static final String[] COLOR_PALETTE = new String[] {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
        "#FFEAA7", "#74B9FF", "#A29BFE", "#FD79A8",
        "#2E8B57", "#1E90FF", "#FF6347", "#9C27B0"
    };

    public ScheduleService(ScheduleRepository scheduleRepository,
                           CurrentUserService currentUserService,
                           AiPlanningService aiPlanningService) {
        this.scheduleRepository = scheduleRepository;
        this.currentUserService = currentUserService;
        this.aiPlanningService = aiPlanningService;
    }

    @Transactional
    public ScheduleResponse createManual(ScheduleCreateRequest req) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        validateDates(req.getStartDate(), req.getEndDate());

        Schedule schedule = new Schedule();
        schedule.setCreatorUserId(user.getUserId());
        schedule.setTeamId(req.getTeamId());
        schedule.setTitle(req.getTitle());
        schedule.setStartDate(req.getStartDate());
        schedule.setEndDate(req.getEndDate());
    schedule.setColor(getOrGenerateColor(req.getColor()));

        if (req.getFloors() != null && !req.getFloors().isEmpty()) {
            for (var f : req.getFloors()) {
                FloorPlan floor = new FloorPlan();
                floor.setCreatorUserId(user.getUserId());
                floor.setTitle(f.getTitle());
                floor.setScheduledDate(f.getScheduledDate());
                schedule.addFloor(floor);
            }
        }
        Schedule saved = scheduleRepository.save(schedule);
        return toResponse(saved);
    }

    @Transactional
    public ScheduleResponse createWithAi(AiScheduleRequest req) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        validateDates(req.getStartDate(), req.getEndDate());

        // Call AI to get suggested floors
        List<AiPlanningService.AiFloor> aiFloors = aiPlanningService.plan(req.getGoal(), req.getStartDate(), req.getEndDate());

        Schedule schedule = new Schedule();
        schedule.setCreatorUserId(user.getUserId());
        schedule.setTeamId(req.getTeamId());
        schedule.setTitle(req.getGoal());
        schedule.setStartDate(req.getStartDate());
        schedule.setEndDate(req.getEndDate());
    schedule.setColor(getOrGenerateColor(req.getColor()));

        for (var af : aiFloors) {
            FloorPlan floor = new FloorPlan();
            floor.setCreatorUserId(user.getUserId());
            floor.setTitle(af.title());
            floor.setScheduledDate(af.date());
            schedule.addFloor(floor);
        }
        Schedule saved = scheduleRepository.save(schedule);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getById(Long id) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        Schedule s = scheduleRepository.findByScheduleIdAndCreatorUserId(id, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        return toResponse(s);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid date range");
        }
    }

    // 사용자가 색상을 제공하지 않은 경우, 팔레트에서 랜덤 색상을 선택
    private String getOrGenerateColor(String userColor) {
        if (userColor != null && !userColor.isBlank()) {
            return userColor;
        }
        return generateRandomColor();
    }

    private String generateRandomColor() {
        int idx = ThreadLocalRandom.current().nextInt(COLOR_PALETTE.length);
        return COLOR_PALETTE[idx];
    }

    private ScheduleResponse toResponse(Schedule s) {
        List<ScheduleResponse.FloorDto> floors = s.getFloors().stream()
                .map(f -> ScheduleResponse.FloorDto.builder()
                        .floorId(f.getFloorId())
                        .title(f.getTitle())
                        .scheduledDate(f.getScheduledDate())
                        .build())
                .collect(Collectors.toList());

        return ScheduleResponse.builder()
                .scheduleId(s.getScheduleId())
                .title(s.getTitle())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .color(s.getColor())
                .teamId(s.getTeamId())
                .floors(floors)
                .build();
    }
}
