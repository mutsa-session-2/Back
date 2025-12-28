package floorida.example.floorida.Item.service;

import floorida.example.floorida.jhh.entity.User;
import floorida.example.floorida.jhh.entity.UserProfile;
import floorida.example.floorida.jhh.repository.UserProfileRepository;
import floorida.example.floorida.jhh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void deductCoin(Long userId, int price) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 프로필 없음"));

        if (profile.getCoin() < price) {
            throw new IllegalStateException("코인이 부족합니다.");
        }

        profile.deductCoin(price); // coin -= price
    }
}
