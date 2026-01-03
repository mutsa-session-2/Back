package floorida.example.floorida.teamBoard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TeamBoardCommentCreateResponse {

    private Long commentId;
    private LocalDateTime createdAt;
}