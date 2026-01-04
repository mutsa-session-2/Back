package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamFloorCancelResponse {
    private boolean alreadyIncomplete;
    private boolean levelDown;
}
