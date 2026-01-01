package floorida.example.floorida.config;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.entity.UserProfile;
import floorida.example.floorida.repository.UserProfileRepository;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.service.CharacterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.test-user.enabled", havingValue = "true")
public class TestUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CharacterService characterService;

    @Value("${app.seed.test-user.email:test1000@floorida.local}")
    private String email;

    @Value("${app.seed.test-user.username:test1000}")
    private String username;

    @Value("${app.seed.test-user.password:test1234!}")
    private String password;

    @Value("${app.seed.test-user.points:1000}")
    private int points;

    public TestUserSeeder(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            CharacterService characterService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.characterService = characterService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            if (userRepository.existsByUsername(username)) {
                throw new IllegalStateException("Test user username already exists: " + username + ". Change app.seed.test-user.username");
            }

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            user = userRepository.save(newUser);

            // 회원가입과 동일하게 기본 캐릭터 보장
            characterService.createDefaultCharacter(user);
        }

        UserProfile profile = userProfileRepository.findById(user.getUserId()).orElse(null);
        if (profile == null) {
            UserProfile p = new UserProfile();
            p.setUser(user);
            p.setPoints(points);
            p.setPersonalLevel(1);
            userProfileRepository.save(p);
        } else {
            // 이미 있다면 테스트 편의를 위해 포인트를 원하는 값으로 맞춤
            profile.setPoints(points);
        }
    }
}
