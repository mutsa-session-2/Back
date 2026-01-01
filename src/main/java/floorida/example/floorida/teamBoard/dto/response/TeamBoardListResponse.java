package floorida.example.floorida.teamBoard.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class TeamBoardListResponse {

    private Long boardId;
    private String content;

    private Long userId;
    private String username;

    private int likeCount;
    private long commentCount;

    private Instant createdAt;
}