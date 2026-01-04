package floorida.example.floorida.teamBoard.entity;

import floorida.example.floorida.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글의 댓글인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private TeamBoard board;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 댓글 내용
    @Column(nullable = false, length = 500)
    private String content;

    // 생성 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 생성자
    public Comment(TeamBoard board, User user, String content) {
        this.board = board;
        this.user = user;
        this.content = content;
    }

    // 댓글 수정
    public void updateContent(String content) {
        this.content = content;
    }

    // insert 전 자동 실행
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // update 전 자동 실행
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}