package floorida.example.floorida.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.entity.UserProfile;
import floorida.example.floorida.repository.UserProfileRepository;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * 처음 로그인한 사용자라면 UserProfile을 생성하고
     * 가입 보너스로 50코인을 지급합니다.
     */
    @Transactional
    public void ensureSignupBonusOnFirstLogin(User user) {
        userProfileRepository.findById(user.getUserId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    profile.setPoints(50); // 가입 + 첫 로그인 보너스
                    profile.setPersonalLevel(1);
                    return userProfileRepository.save(profile);
                });
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


