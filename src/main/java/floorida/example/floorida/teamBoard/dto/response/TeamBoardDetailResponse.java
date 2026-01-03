package floorida.example.floorida.teamBoard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeamBoardDetailResponse {

    private Long boardId;
    private Long teamId;

    private String title;
    private String content;

    private Long writerId;
    private String writerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
