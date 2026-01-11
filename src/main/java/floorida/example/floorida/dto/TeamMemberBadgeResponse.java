package floorida.example.floorida.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamMemberBadgeResponse {
    private Long userId;
    private String username;
    private List<MyBadgeResponse> equippedBadges;

    public static TeamMemberBadgeResponse of(Long userId, String username, List<MyBadgeResponse> equippedBadges) {
        return TeamMemberBadgeResponse.builder()
                .userId(userId)
                .username(username)
                .equippedBadges(equippedBadges)
                .build();
    }
}
