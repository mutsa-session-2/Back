package floorida.example.floorida.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.dto.LoginRequest;
import floorida.example.floorida.dto.SignupRequest;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CharacterService characterService;
    private final UserProfileService userProfileService;
    private final BadgeService badgeService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CharacterService characterService,
                       UserProfileService userProfileService,
                       BadgeService badgeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.characterService = characterService;
        this.userProfileService = userProfileService;
        this.badgeService = badgeService;
    }

    @Transactional
    public User register(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        // 신규 가입자는 이메일 인증 전까지 로그인 불가
        user.setEmailVerified(Boolean.FALSE);
        User savedUser = userRepository.save(user);
        
        // 회원가입 시 기본 캐릭터 자동 생성
        characterService.createDefaultCharacter(savedUser);
        
        return savedUser;
    }

    public record LoginResult(User user, boolean dailyRewardGiven, boolean firstLoginBonusGiven) {}

    public LoginResult authenticateOrThrow(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // 이메일 인증이 명시적으로 false인 경우만 차단 (기존 유저 null은 통과)
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("Email not verified");
        }

        // 처음 로그인한 사용자라면 UserProfile 생성 + 50코인 지급
        boolean createdProfile = userProfileService.ensureSignupBonusOnFirstLogin(user);

        // 접속일 기준 하루 1회 일일 접속 보상 (+10코인)
        // 첫 로그인(가입 보너스 50코인) 날에도 함께 지급되도록 분리 정책으로 처리합니다.
        boolean dailyRewardGiven = userProfileService.grantDailyLoginRewardOnLogin(user.getUserId(), LocalDate.now());

        // 로그인 기반 연속 출석 streak에 따라 출석 뱃지를 지급합니다.
        int streak = userProfileService.getCurrentDailyLoginStreak(user.getUserId(), LocalDate.now());
        badgeService.onDailyLoginAttendance(user, streak);

        return new LoginResult(user, dailyRewardGiven, createdProfile);
    }

    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String trimmed = newUsername.trim();
        if (trimmed.equals(user.getUsername())) {
            return user;
        }

        if (userRepository.existsByUsername(trimmed)) {
            throw new IllegalArgumentException("Username already in use");
        }

        user.setUsername(trimmed);
        return userRepository.save(user);
    }
}
