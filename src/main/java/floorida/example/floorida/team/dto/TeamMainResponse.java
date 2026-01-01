package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamMainResponse {
    private Long teamId;
    private String name;
    private String description;
    private Integer level;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;

    private List<Long> membersIds;

}
