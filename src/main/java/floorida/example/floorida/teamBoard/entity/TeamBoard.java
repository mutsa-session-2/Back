package floorida.example.floorida.teamBoard.entity;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.team.entity.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_board")
@Getter
@NoArgsConstructor
public class TeamBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팀
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 게시글 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 좋아요 수
    @Column(nullable = false)
    private int likeCount = 0;

    // 생성 시간 (DB 자동)
    @Column(
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime createdAt;

    // 수정 시간 (DB 자동)
    @Column(
            nullable = false,
            columnDefinition =
                    "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
    )
    private LocalDateTime updatedAt;

    // 생성자
    public TeamBoard(Team team, User user, String content) {
        this.team = team;
        this.user = user;
        this.content = content;
    }

    // 내용 수정
    public void updateContent(String content) {
        this.content = content;
    }

    // 좋아요 처리
    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}