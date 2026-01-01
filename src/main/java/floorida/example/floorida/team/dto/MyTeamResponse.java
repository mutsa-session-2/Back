package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MyTeamResponse {
    private Long teamId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
