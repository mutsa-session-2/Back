package floorida.example.floorida.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeamJoinRequest {
    @NotBlank
    private String joinCode;
}
