package floorida.example.floorida.team.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamFloorResponse {
    private Long teamFloorId;
    private Long teamId;
    private String title;
    private LocalDate dueDate;

    private boolean completed;
    private Instant completedAt;

    private List<Long> assigneeUserIds;
}
