package floorida.example.floorida.teamBoard.repository;

import java.time.Instant;
import java.time.LocalDateTime;

public interface TeamBoardListView {

    Long getId();
    String getContent();
    int getLikeCount();
    Instant getCreatedAt();

    Long getUserId();
    String getUsername();

    Long getCommentCount();
}
