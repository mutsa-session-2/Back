package floorida.example.floorida.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.MyBadgeResponse;
import floorida.example.floorida.entity.Badge;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.entity.UserBadge;
import floorida.example.floorida.repository.BadgeRepository;
import floorida.example.floorida.repository.FloorStatusRepository;
import floorida.example.floorida.repository.UserBadgeRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BadgeService {

    // 연속 출석 뱃지 기준 (업로드된 이미지 기준)
    private static final List<Integer> ATTENDANCE_MILESTONES = List.of(1, 7, 30, 200, 300, 400, 500);

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final FloorStatusRepository floorStatusRepository;
    private final CurrentUserService currentUserService;

    public BadgeService(
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            FloorStatusRepository floorStatusRepository,
            CurrentUserService currentUserService
    ) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.floorStatusRepository = floorStatusRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<MyBadgeResponse> getMyBadges() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        return userBadgeRepository.findAllWithBadgeByUserId(user.getUserId()).stream()
                .map(ub -> MyBadgeResponse.builder()
                        .badgeId(ub.getBadge().getBadgeId())
                        .name(ub.getBadge().getName())
                        .type(ub.getBadge().getType())
                        .description(ub.getBadge().getDescription())
                        .imageUrl(ub.getBadge().getImageUrl())
                .offsetX(ub.getBadge().getOffsetX())
                .offsetY(ub.getBadge().getOffsetY())
                .width(ub.getBadge().getWidth())
                .height(ub.getBadge().getHeight())
                        .earnedAt(ub.getEarnedAt())
                        .equipped(ub.isEquipped())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyBadgeResponse> getMyEquippedBadges() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        return userBadgeRepository.findEquippedBadgesByUserId(user.getUserId()).stream()
                .map(ub -> MyBadgeResponse.builder()
                        .badgeId(ub.getBadge().getBadgeId())
                        .name(ub.getBadge().getName())
                        .type(ub.getBadge().getType())
                        .description(ub.getBadge().getDescription())
                        .imageUrl(ub.getBadge().getImageUrl())
                .offsetX(ub.getBadge().getOffsetX())
                .offsetY(ub.getBadge().getOffsetY())
                .width(ub.getBadge().getWidth())
                .height(ub.getBadge().getHeight())
                        .earnedAt(ub.getEarnedAt())
                        .equipped(true)
                        .build())
                .toList();
    }

    /**
     * 내 뱃지 장착.
     * - 사용자가 보유한 뱃지만 장착 가능
     * - 기본 정책: 유저당 1개만 장착(기존 장착 뱃지는 해제)
     */
    @Transactional
    public void equipMyBadge(Long badgeId) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        if (badgeId == null) {
            throw new IllegalArgumentException("badgeId is required");
        }

        UserBadge target = userBadgeRepository.findById_UserIdAndId_BadgeId(user.getUserId(), badgeId)
                .orElseThrow(() -> new EntityNotFoundException("badge not found"));

        if (target.isEquipped()) {
            return;
        }

        List<UserBadge> equippedBadges = userBadgeRepository.findEquippedBadgesByUserId(user.getUserId());
        for (UserBadge ub : equippedBadges) {
            ub.setEquipped(false);
        }

        target.setEquipped(true);
    }

    /**
     * 내 뱃지 해제.
     * - 사용자가 보유한 뱃지만 해제 가능
     */
    @Transactional
    public void unequipMyBadge(Long badgeId) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        if (badgeId == null) {
            throw new IllegalArgumentException("badgeId is required");
        }

        Optional<UserBadge> opt = userBadgeRepository.findById_UserIdAndId_BadgeId(user.getUserId(), badgeId);
        if (opt.isEmpty()) {
            throw new EntityNotFoundException("badge not found");
        }

        UserBadge ub = opt.get();
        ub.setEquipped(false);
    }

    /**
     * 출석 기준(가정): 하루에 Floor 1개 이상 완료하면 출석 처리.
     * 완료 처리된 날짜(attendanceDate)를 기준으로 연속 출석(streak)을 계산하고, 기준일에 도달하면 뱃지를 지급합니다.
     */
    @Transactional
    public void onAttendance(User user, LocalDate attendanceDate) {
        if (user == null) {
            return;
        }
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }

        int maxDays = ATTENDANCE_MILESTONES.get(ATTENDANCE_MILESTONES.size() - 1);
        LocalDate start = attendanceDate.minusDays(maxDays - 1);

        Set<LocalDate> attendedDates = new HashSet<>(
                floorStatusRepository.findDistinctCompletedScheduledDates(user.getUserId(), start, attendanceDate)
        );

        int streak = 0;
        LocalDate cursor = attendanceDate;
        while (attendedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        if (!ATTENDANCE_MILESTONES.contains(streak)) {
            return;
        }

        String badgeName = streak + "일출석";
        Badge badge = badgeRepository.findByName(badgeName)
                .orElse(null);
        if (badge == null) {
            return;
        }

        if (userBadgeRepository.existsById_UserIdAndId_BadgeId(user.getUserId(), badge.getBadgeId())) {
            return;
        }

        UserBadge ub = new UserBadge(user, badge);
        userBadgeRepository.save(ub);
    }

    /**
     * 일일 접속 보상(로그인) 기준 연속 출석(streak)로 출석 뱃지를 지급합니다.
     * - streak 값은 이미 계산/저장된 값을 받아 사용합니다.
     * - 같은 뱃지는 중복 지급하지 않습니다.
     */
    @Transactional
    public void onDailyLoginAttendance(User user, Integer streak) {
        if (user == null) {
            return;
        }
        if (streak == null || streak <= 0) {
            return;
        }

        if (!ATTENDANCE_MILESTONES.contains(streak)) {
            return;
        }

        String badgeName = streak + "일출석";
        Badge badge = badgeRepository.findByName(badgeName)
                .orElse(null);
        if (badge == null) {
            return;
        }

        if (userBadgeRepository.existsById_UserIdAndId_BadgeId(user.getUserId(), badge.getBadgeId())) {
            return;
        }

        UserBadge ub = new UserBadge(user, badge);
        userBadgeRepository.save(ub);
    }
}
