package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamMemberResponse {

    @Schema(example = "1")
    private Long userId;

    @Schema(example = "테스트유저")
    private String username;

    @Schema(example = "owner")
    private String role;
}
