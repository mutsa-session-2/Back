package floorida.example.floorida.service;

import floorida.example.floorida.dto.CharacterResponse;
import floorida.example.floorida.entity.Character;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    private final CurrentUserService currentUserService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public CharacterResponse getMyCharacter() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
        
        Character character = characterRepository.findByUser_UserId(user.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("캐릭터가 존재하지 않습니다"));
        
        return new CharacterResponse(
            character.getCharacterId(),
            character.getImageUrl()
        );
    }

    @Transactional
    public void createDefaultCharacter(User user) {
        // 이미 캐릭터가 있으면 생성하지 않음
        if (characterRepository.existsByUser_UserId(user.getUserId())) {
            return;
        }

        // S3 버킷의 기본 캐릭터 이미지 URL
        String defaultImageUrl = String.format(
            "https://%s.s3.%s.amazonaws.com/캐릭터.png",
            bucketName,
            region
        );

        Character character = Character.builder()
            .user(user)
            .imageUrl(defaultImageUrl)
            .build();
        
        characterRepository.save(character);
    }
}
