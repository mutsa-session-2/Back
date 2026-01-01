package floorida.example.floorida.teamBoard.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamBoardCreateResponse {

    private Long boardId;
    private Instant createdAt;
}