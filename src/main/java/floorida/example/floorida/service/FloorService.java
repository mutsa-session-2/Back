package floorida.example.floorida.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.FloorCreateRequest;
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
    public record CompleteResult(Long floorId, boolean completed, int coinsAwarded, int currentPoints, Instant completedAt) {
    }

    @Transactional
    public CompleteResult completeFloor(Long floorId) {
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
        Instant completedAt = Instant.now();
        status.setCompletedAt(completedAt);
        floorStatusRepository.save(status);

        // 코인 지급 (지난 날짜가 아닐 때만 10코인)
        LocalDate today = LocalDate.now();
        boolean isOverdue = floor.getScheduledDate() != null && floor.getScheduledDate().isBefore(today);
        int coinsToAward = isOverdue ? 0 : 10;
        
        if (coinsToAward > 0) {
            userProfileService.addPoints(user.getUserId(), coinsToAward);
        }

        int currentPoints = userProfileService.getPoints(user.getUserId());

        // 개인 층수 +1 (지각이어도 층수는 올라감)
        userProfileService.incrementPersonalLevel(user.getUserId());

        // 출석 뱃지 지급
        LocalDate attendanceDate = floor.getScheduledDate() != null ? floor.getScheduledDate() : today;
        badgeService.onAttendance(user, attendanceDate);

        return new CompleteResult(floorId, true, coinsToAward, currentPoints, completedAt);
    }

    /**
     * Floor 완료 취소 처리 (잘못 눌렀을 때 되돌리기)
     */
    public record UncompleteResult(Long floorId, boolean completed, int coinsDeducted, int currentPoints) {
    }

    @Transactional
    public UncompleteResult uncompleteFloor(Long floorId) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        FloorPlan floor = floorPlanRepository.findById(floorId)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found"));

        // 본인이 생성한 Floor인지 확인
        if (!floor.getCreatorUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Not authorized to uncomplete this floor");
        }

        // 완료된 Floor인지 확인
        FloorStatus status = floorStatusRepository.findByFloor_FloorIdAndUser_UserId(floorId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Floor is not completed"));

        // 코인 차감 판단
        // - 완료 당시(completedAt) 기준으로 지각이었는지 확인해야 함
        int coinsToDeduct = 0;
        if (floor.getScheduledDate() != null && status.getCompletedAt() != null) {
            LocalDate completedDate = status.getCompletedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            // 완료한 날짜가 예정일보다 뒤라면 지각처리 되었었음 (코인 0)
            boolean wasOverdue = completedDate.isAfter(floor.getScheduledDate());
            coinsToDeduct = wasOverdue ? 0 : 10;
        } else {
            // 날짜 정보 없으면 기본 10으로 간주
            coinsToDeduct = 10;
        }
        
        // FloorStatus 삭제 (완료 취소)
        floorStatusRepository.delete(status);

        // 코인 차감 (가지고 있는 만큼만)
        int currentPoints = userProfileService.getPoints(user.getUserId());
        int realDeduction = 0;
        
        if (coinsToDeduct > 0) {
            realDeduction = Math.min(currentPoints, coinsToDeduct);
            if (realDeduction > 0) {
                userProfileService.deductPoints(user.getUserId(), realDeduction);
            }
        }

        int newPoints = userProfileService.getPoints(user.getUserId());

        // 개인 층수 -1 (최소 1층 유지)
        userProfileService.decrementPersonalLevel(user.getUserId());

        return new UncompleteResult(floorId, false, realDeduction, newPoints);
    }

    /**
     * Floor 생성 (세부 일정 추가)
     * - 같은 날짜에 여러 개 생성 가능 (중복 체크 X)
     * - 일정(Schedule) 기간 내에 포함되어야 함
     */
    @Transactional
    public FloorResponse createFloor(FloorCreateRequest req) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        Schedule schedule = scheduleRepository.findByScheduleIdAndCreatorUserId(req.getScheduleId(), user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        
        // 날짜 유효성 체크 (일정 기간 내인지)
        if (schedule.getStartDate() != null && schedule.getEndDate() != null) {
            if (req.getScheduledDate().isBefore(schedule.getStartDate()) || req.getScheduledDate().isAfter(schedule.getEndDate())) {
                throw new IllegalArgumentException("scheduledDate must be within schedule date range");
            }
        }
        
        FloorPlan floor = new FloorPlan();
        floor.setCreatorUserId(user.getUserId());
        floor.setSchedule(schedule);
        floor.setTitle(req.getTitle());
        floor.setScheduledDate(req.getScheduledDate());
        
        // 생성일/수정일 등 필요한 경우 설정 (Entity 리스너가 없다면)
        // floor.setCreatedAt(Instant.now()); 
        
        FloorPlan saved = floorPlanRepository.save(floor);
        return toResponse(saved);
    }

    /**
     * Floor 제목/날짜 수정
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

            // 중복 체크 로직 제거됨 (하루에 여러 개 허용)
            // Long scheduleId = ... (삭제)
            // if (scheduleId != null) ... (삭제)

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
