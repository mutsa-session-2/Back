package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamMemberResponse {
    private Long userId;
    private String role;
}
