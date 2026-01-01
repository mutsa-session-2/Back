package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TeamResponse {
    private Long teamId;
    private String name;
    private Integer level;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
}
