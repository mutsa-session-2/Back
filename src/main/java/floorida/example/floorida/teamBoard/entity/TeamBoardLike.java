package floorida.example.floorida.teamBoard.entity;

import floorida.example.floorida.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "team_board_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "user_id"})
)
@Getter
@NoArgsConstructor
public class TeamBoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id")
    private TeamBoard board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public TeamBoardLike(TeamBoard board, User user) {
        this.board = board;
        this.user = user;
    }
}