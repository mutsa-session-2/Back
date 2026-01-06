package floorida.example.floorida.service;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.entity.UserProfile;
import floorida.example.floorida.repository.UserProfileRepository;

@Service
public class UserProfileService {

    private static final int DAILY_LOGIN_REWARD_POINTS = 10;

    private final UserProfileRepository userProfileRepository;
    private final EntityManager entityManager;

    public UserProfileService(UserProfileRepository userProfileRepository, EntityManager entityManager) {
        this.userProfileRepository = userProfileRepository;
        this.entityManager = entityManager;
    }

    /**
     * 처음 로그인한 사용자라면 UserProfile을 생성하고
     * 가입 보너스로 50코인을 지급합니다.
     */
    @Transactional
    public boolean ensureSignupBonusOnFirstLogin(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        boolean exists = userProfileRepository.existsById(user.getUserId());
        if (exists) {
            return false;
        }

        UserProfile profile = new UserProfile();
        // @MapsId만 믿으면 환경/상태에 따라 userId가 null로 남아
        // 저장 시 'ids must be manually assigned' 류의 예외가 발생할 수 있어 명시적으로 세팅합니다.
        profile.setUserId(user.getUserId());
        // authenticateOrThrow()에서 읽어온 User는 이 트랜잭션 밖에서 로드되어 detached일 수 있으므로
        // persist 시 문제가 생기지 않게 영속 상태 참조로 연결합니다.
        User managedUserRef = entityManager.getReference(User.class, user.getUserId());
        profile.setUser(managedUserRef);
        profile.setPoints(50); // 가입 + 첫 로그인 보너스
        profile.setPersonalLevel(1);
        // PK가 이미 채워진 엔티티를 save()하면 merge()를 타면서
        // 'unsaved-value mapping was incorrect' / StaleObjectStateException 이 날 수 있습니다.
        // 신규 생성 케이스는 persist()로 명시적으로 INSERT 합니다.
        entityManager.persist(profile);
        return true;
    }

    /**
     * 로그인 성공 시, 접속일 기준으로 하루 1회만 일일 접속 보상(10코인)을 지급합니다.
     * - 회원가입 직후 첫 로그인 보너스(50코인)와는 별개 정책이므로, 호출 위치에서 필요 시 제외하세요.
     */
    @Transactional
    public boolean grantDailyLoginRewardOnLogin(Long userId, LocalDate loginDate) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (loginDate == null) {
            loginDate = LocalDate.now();
        }

        // Legacy/운영 데이터 호환: profile이 없는 기존 유저도 있을 수 있으므로 기본 프로필을 생성합니다.
        ensureProfileExistsWithoutSignupBonus(userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        LocalDate last = profile.getLastDailyLoginRewardDate();
        if (loginDate.equals(last)) {
            return false;
        }

        int prevStreak = profile.getDailyLoginStreak() != null ? profile.getDailyLoginStreak() : 0;
        int nextStreak;
        if (last == null) {
            nextStreak = 1;
        } else if (loginDate.equals(last.plusDays(1))) {
            nextStreak = prevStreak + 1;
        } else {
            nextStreak = 1;
        }

        profile.setPoints(profile.getPoints() + DAILY_LOGIN_REWARD_POINTS);
        profile.setLastDailyLoginRewardDate(loginDate);
        profile.setDailyLoginStreak(nextStreak);
        return true;
    }

    /**
     * 현재(오늘 기준) 유지 중인 연속 출석 일수를 반환합니다.
     * - 오늘 접속 보상을 이미 받았으면 그 값을 반환
     * - 아직 오늘 보상을 받지 않았지만 어제가 마지막 접속일이면(오늘 접속 전) 그 값을 반환
     * - 그 외에는 0
     */
    @Transactional(readOnly = true)
    public int getCurrentDailyLoginStreak(Long userId, LocalDate today) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (today == null) {
            today = LocalDate.now();
        }

        // Legacy/운영 데이터 호환
        ensureProfileExistsWithoutSignupBonus(userId);

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        LocalDate last = profile.getLastDailyLoginRewardDate();
        int streak = profile.getDailyLoginStreak() != null ? profile.getDailyLoginStreak() : 0;
        if (last == null) {
            return 0;
        }
        if (today.equals(last) || today.minusDays(1).equals(last)) {
            return Math.max(streak, 0);
        }
        return 0;
    }

    @Transactional
    public void ensureProfileExistsWithoutSignupBonus(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        boolean exists = userProfileRepository.existsById(userId);
        if (exists) {
            return;
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        User managedUserRef = entityManager.getReference(User.class, userId);
        profile.setUser(managedUserRef);
        profile.setPoints(0);
        profile.setPersonalLevel(1);
        // streak 관련 필드는 기본값(0/null) 그대로 두고 생성
        entityManager.persist(profile);
    }

    /**
     * 온보딩에서 받은 성향 정보를 저장/업데이트합니다.
     * (회원가입 직후가 아니어도 언제든지 호출 가능)
     */
    @Transactional
    public UserProfile updateOnboarding(Long userId, String planningTendency, String dailyStudyHours) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        if (planningTendency != null && !planningTendency.isBlank()) {
            profile.setPlanningTendency(planningTendency);
        }
        if (dailyStudyHours != null && !dailyStudyHours.isBlank()) {
            profile.setDailyStudyHours(dailyStudyHours);
        }
        return profile;
    }

    /** 포인트 추가 */
    @Transactional
    public void addPoints(Long userId, int points) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        profile.setPoints(profile.getPoints() + points);
    }

    /** 포인트 차감 */
    @Transactional
    public void deductPoints(Long userId, int points) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        if (profile.getPoints() < points) {
            throw new IllegalArgumentException("Not enough points");
        }
        profile.setPoints(profile.getPoints() - points);
    }

    /** 현재 포인트 조회 */
    @Transactional(readOnly = true)
    public int getPoints(Long userId) {
        return userProfileRepository.findById(userId)
                .map(UserProfile::getPoints)
                .orElse(0);
    }

    /** 전체 프로필 조회 */
    @Transactional(readOnly = true)
    public UserProfile getProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
    }

    /** 개인 층수 +1 (오늘 할 일 1개 완료 시 증가) */
    @Transactional
    public void incrementPersonalLevel(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        profile.setPersonalLevel(profile.getPersonalLevel() + 1);
    }
}


