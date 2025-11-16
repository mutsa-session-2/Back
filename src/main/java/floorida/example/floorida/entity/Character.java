package floorida.example.floorida.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "characters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Character {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long characterId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    // 현재 장착 중인 아이템 (JSON 문자열로 저장)
    // PostgreSQL JSON 타입 대신 VARCHAR/TEXT로 저장해서 매핑 오류를 피함
    @Column(name = "equipped_items", nullable = true, length = 2000)
    private String equippedItems;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
