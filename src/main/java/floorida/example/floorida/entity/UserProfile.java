package floorida.example.floorida.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserProfile 엔티티
 *
 * - points: 코인(포인트)
 * - personalLevel: 개인 층수
 *
 * 온보딩에서 사용할 planningTendency / dailyStudyHours 는
 * 아직 UI/비즈니스 로직이 없으므로 선택 값으로 두고, 이후 확장 시 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "planning_tendency", length = 50)
    private String planningTendency;

    @Column(name = "daily_study_hours", length = 50)
    private String dailyStudyHours;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "personal_level", nullable = false)
    private Integer personalLevel = 1;
}


