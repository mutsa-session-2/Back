package floorida.example.floorida.team;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long team_id;

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

    @OneToMany(mappedBy = "team")
    private Set<TeamMember> teamMembers = new HashSet<>();

    public Team(String name, Integer level, String description, String joinCode) {
        this.name = name;
        this.level = level;
        this.description = description;
        this.joinCode = joinCode;
    }


    public void increaseLevel() {
        this.level += 1;
    }


}
