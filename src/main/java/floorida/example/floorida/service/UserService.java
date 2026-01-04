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

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CharacterService characterService,
                       UserProfileService userProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.characterService = characterService;
        this.userProfileService = userProfileService;
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
        User savedUser = userRepository.save(user);
        
        // 회원가입 시 기본 캐릭터 자동 생성
        characterService.createDefaultCharacter(savedUser);
        
        return savedUser;
    }

    public User authenticateOrThrow(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // 처음 로그인한 사용자라면 UserProfile 생성 + 50코인 지급
        boolean createdProfile = userProfileService.ensureSignupBonusOnFirstLogin(user);

        // 접속일 기준 하루 1회 일일 접속 보상 (+10코인)
        // 첫 로그인(가입 보너스 50코인) 날에도 함께 지급되도록 분리 정책으로 처리합니다.
        userProfileService.grantDailyLoginRewardOnLogin(user.getUserId(), LocalDate.now());

        return user;
    }
}
