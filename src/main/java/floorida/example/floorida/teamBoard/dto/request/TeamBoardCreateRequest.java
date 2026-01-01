package floorida.example.floorida.teamBoard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TeamBoardCreateRequest {

    @NotBlank
    private String content;
}