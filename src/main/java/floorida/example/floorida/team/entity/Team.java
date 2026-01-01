package floorida.example.floorida.team.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(columnDefinition = "text")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "join_code", nullable = false, unique = true, length = 20)
    private String joinCode;

    // 프로젝트 기간
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private Set<TeamMember> teamMembers = new HashSet<>();

    public Team(String name, String description,
                LocalDate startDate, LocalDate endDate, String joinCode) {

        this.name = name;
        this.description = description; //사용 X, team name만으로 가기로
        this.joinCode = joinCode;
        this.level = 1;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void increaseLevel() {
        this.level += 1;
    }


}
