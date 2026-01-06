package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "팀 할 일 상세 조회 응답")
public record TeamFloorDetailResponse(

        @Schema(description = "팀 할 일 ID", example = "12")
        Long teamFloorId,

        @Schema(description = "팀 ID", example = "7")
        Long teamId,

        @Schema(description = "할 일 제목", example = "DFS/BFS 학습")
        String title,

        @Schema(description = "마감일", example = "2026-01-10")
        LocalDate dueDate,

        @Schema(description = "완료 여부", example = "false")
        boolean completed,

        @Schema(description = "완료 시각 (완료되지 않은 경우 null)",
                example = "2026-01-04T08:57:21.601027Z")
        Instant completedAt,

        @Schema(description = "배정된 사용자 ID 목록", example = "[36, 42]")
        List<Long> assigneeUserIds,

        @Schema(description = "배정된 사용자 정보 목록")
        List<AssigneeInfo> assignees
) {

    @Schema(description = "배정자 정보")
    public record AssigneeInfo(
            @Schema(description = "사용자 ID", example = "36")
            Long userId,

            @Schema(description = "사용자 username", example = "test1234")
            String username
    ) {}
}
