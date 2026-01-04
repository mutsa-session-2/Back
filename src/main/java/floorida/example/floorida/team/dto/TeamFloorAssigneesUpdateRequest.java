package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeamFloorAssigneesUpdateRequest {
    private List<Long> assigneeUserIds;
}
