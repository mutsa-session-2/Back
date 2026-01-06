package floorida.example.floorida.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.FloorResponse;
import floorida.example.floorida.dto.FloorUpdateRequest;
import floorida.example.floorida.entity.FloorPlan;
import floorida.example.floorida.entity.FloorStatus;
import floorida.example.floorida.entity.Schedule;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.FloorPlanRepository;
import floorida.example.floorida.repository.FloorStatusRepository;
import floorida.example.floorida.repository.ScheduleRepository;

@Service
public class FloorService {
    private final FloorPlanRepository floorPlanRepository;
    private final FloorStatusRepository floorStatusRepository;
    private final ScheduleRepository scheduleRepository;
    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;
    private final BadgeService badgeService;

    public FloorService(FloorPlanRepository floorPlanRepository,
                        FloorStatusRepository floorStatusRepository,
                        ScheduleRepository scheduleRepository,
                        CurrentUserService currentUserService,
                        UserProfileService userProfileService,
                        BadgeService badgeService) {
        this.floorPlanRepository = floorPlanRepository;
        this.floorStatusRepository = floorStatusRepository;
        this.scheduleRepository = scheduleRepository;
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
        this.badgeService = badgeService;
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

        // 출석 뱃지 지급 (가정: 하루 1개 이상 완료 = 출석)
        LocalDate attendanceDate = floor.getScheduledDate() != null ? floor.getScheduledDate() : LocalDate.now();
        badgeService.onAttendance(user, attendanceDate);
    }

    /**
     * Floor 제목 수정
     */
    @Transactional
    public FloorResponse updateFloor(Long floorId, FloorUpdateRequest req) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        FloorPlan floor = floorPlanRepository.findById(floorId)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found"));

        if (!floor.getCreatorUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Not authorized to update this floor");
        }

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            floor.setTitle(req.getTitle());
        }

        if (req.getScheduledDate() != null) {
            var schedule = floor.getSchedule();
            if (schedule != null && schedule.getStartDate() != null && schedule.getEndDate() != null) {
                if (req.getScheduledDate().isBefore(schedule.getStartDate()) || req.getScheduledDate().isAfter(schedule.getEndDate())) {
                    throw new IllegalArgumentException("scheduledDate must be within schedule date range");
                }
            }

            Long scheduleId = schedule != null ? schedule.getScheduleId() : null;
            if (scheduleId != null) {
                List<FloorPlan> sameDateFloors = floorPlanRepository
                        .findBySchedule_ScheduleIdAndScheduledDate(scheduleId, req.getScheduledDate());
                boolean hasOther = sameDateFloors.stream()
                        .anyMatch(f -> f.getFloorId() != null && !f.getFloorId().equals(floorId));
                if (hasOther) {
                    throw new IllegalArgumentException("Another floor already exists for the given scheduledDate");
                }
            }

            floor.setScheduledDate(req.getScheduledDate());
        }

        FloorPlan saved = floorPlanRepository.save(floor);
        return toResponse(saved);
    }

    /**
     * Floor 삭제
     * - FloorStatus가 남아있으면 FK 제약으로 500이 발생할 수 있으므로 선삭제합니다.
     */
    @Transactional
    public void deleteFloor(Long floorId) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        FloorPlan floor = floorPlanRepository.findById(floorId)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found"));

        if (!floor.getCreatorUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Not authorized to delete this floor");
        }

        floorStatusRepository.deleteByFloorId(floorId);
        floorPlanRepository.delete(floor);
    }

    /**
     * 일정(scheduleId)에 속한 floors 삭제 요청
     * - floors만 지우면 일정이 의미가 없으므로, 일정 자체를 삭제합니다.
     * - FloorStatus는 FK 제약으로 인해 먼저 정리합니다.
     */
    @Transactional
    public void deleteFloorsBySchedule(Long scheduleId) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        Schedule schedule = scheduleRepository.findByScheduleIdAndCreatorUserId(scheduleId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        floorStatusRepository.deleteByScheduleId(scheduleId);
        scheduleRepository.delete(schedule);
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
