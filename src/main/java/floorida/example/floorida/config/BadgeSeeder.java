package floorida.example.floorida.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.entity.Badge;
import floorida.example.floorida.repository.BadgeRepository;

@Component
public class BadgeSeeder implements CommandLineRunner {

    private static final String BADGE_BASE_URL = "https://bucket-gc5ukj.s3.us-east-1.amazonaws.com/%EB%B1%83%EC%A7%80/";

    private final BadgeRepository badgeRepository;

    public BadgeSeeder(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Integer> milestones = List.of(1, 70, 30, 200, 300, 400, 500);

        for (int days : milestones) {
            String name = days + "일출석";
            if (badgeRepository.existsByName(name)) {
                continue;
            }

            Badge b = new Badge();
            b.setName(name);
            b.setType("INDIVIDUAL");
            b.setDescription("연속 " + days + "일 출석 달성");
            b.setImageUrl(BADGE_BASE_URL + days + "%EC%9D%BC%EC%B6%9C%EC%84%9D.png");
            badgeRepository.save(b);
        }
    }
}
