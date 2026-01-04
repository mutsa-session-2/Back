package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamFloorCompleteResponse {
    private boolean alreadyCompleted;
    private boolean levelUp;
}
