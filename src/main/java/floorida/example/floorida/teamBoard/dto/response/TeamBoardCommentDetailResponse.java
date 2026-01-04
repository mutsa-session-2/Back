package floorida.example.floorida.teamBoard.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeamBoardCommentDetailResponse {

    private Long commentId;

    private Long boardId;

    private Long writerId;
    private String writerName;

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}