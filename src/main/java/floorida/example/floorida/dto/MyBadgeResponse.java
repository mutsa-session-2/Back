package floorida.example.floorida.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyBadgeResponse {
    private Long badgeId;
    private String name;
    private String type;
    private String description;
    private String imageUrl;
    private Instant earnedAt;
}
