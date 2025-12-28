package floorida.example.floorida.Item.service;

import floorida.example.floorida.entity.UserProfile;
import floorida.example.floorida.repository.UserProfileRepository;
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
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 프로필 없음"));

        profile.deductPoints(price);
    }
}
