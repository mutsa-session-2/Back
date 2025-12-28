package floorida.example.floorida.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.entity.Schedule;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.CharacterRepository;
import floorida.example.floorida.repository.FloorStatusRepository;
import floorida.example.floorida.repository.ScheduleRepository;
import floorida.example.floorida.repository.UserBadgeRepository;
import floorida.example.floorida.repository.UserProfileRepository;
import floorida.example.floorida.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CharacterRepository characterRepository;
    private final FloorStatusRepository floorStatusRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            CharacterRepository characterRepository,
            FloorStatusRepository floorStatusRepository,
            ScheduleRepository scheduleRepository,
                UserBadgeRepository userBadgeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.characterRepository = characterRepository;
        this.floorStatusRepository = floorStatusRepository;
        this.scheduleRepository = scheduleRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void withdraw(User user, String rawPassword) {
        if (user == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Long userId = user.getUserId();

        // 1) floor_statuses: user가 찍은 것 + user가 만든 floor에 달린 것(다른 유저 기록 포함)
        floorStatusRepository.deleteAllRelatedToUser(userId);

        // 2) schedules (cascade로 floors 함께 삭제)
        List<Schedule> schedules = scheduleRepository.findByCreatorUserId(userId);
        if (!schedules.isEmpty()) {
            scheduleRepository.deleteAll(schedules);
        }

        // 3) character
        characterRepository.deleteByUser_UserId(userId);

        // 4) user_badges
        userBadgeRepository.deleteAllById_UserId(userId);

        // 5) profile
        userProfileRepository.deleteById(userId);

        // 6) user
        userRepository.deleteById(userId);
    }
}
