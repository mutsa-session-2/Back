package floorida.example.floorida.teamBoard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TeamBoardCommentListResponse {

    private Long commentId;

    private Long writerId;
    private String writerName;

    private String content;

    private LocalDateTime createdAt;
}