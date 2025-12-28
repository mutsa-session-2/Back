package floorida.example.floorida.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserBadgeId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "badge_id")
    private Long badgeId;

    public UserBadgeId(Long userId, Long badgeId) {
        this.userId = userId;
        this.badgeId = badgeId;
    }
}
