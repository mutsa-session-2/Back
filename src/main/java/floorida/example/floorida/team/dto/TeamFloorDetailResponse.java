package floorida.example.floorida.team.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record TeamFloorDetailResponse(
        Long teamFloorId,
        Long teamId,
        String title,
        LocalDate dueDate,
        boolean completed,
        Instant completedAt,
        List<Long> assigneeUserIds
) {}
