package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "팀 할 일 목록 조회 응답")
public class TeamFloorResponse {

    @Schema(description = "팀 할 일 ID", example = "12")
    private Long teamFloorId;

    @Schema(description = "팀 ID", example = "7")
    private Long teamId;

    @Schema(description = "할 일 제목", example = "DFS/BFS 학습")
    private String title;

    @Schema(description = "마감일", example = "2026-01-10")
    private LocalDate dueDate;

    @Schema(description = "완료 여부", example = "false")
    private boolean completed;

    @Schema(
            description = "완료 시각 (완료되지 않은 경우 null)",
            example = "2026-01-04T08:57:21.601027Z"
    )
    private Instant completedAt;

    @ArraySchema(
            schema = @Schema(
                    description = "배정된 사용자 ID",
                    example = "36"
            ),
            arraySchema = @Schema(
                    description = "배정된 사용자 ID 목록"
            )
    )
    private List<Long> assigneeUserIds;

    @ArraySchema(
            arraySchema = @Schema(description = "배정된 사용자 정보 목록"),
            schema = @Schema(implementation = AssigneeInfo.class)
    )
    private List<AssigneeInfo> assignees;

    @Schema(description = "현재 팀 레벨(엘리베이터 층수)", example = "3")
    private Integer teamLevel;

    @Getter
    @AllArgsConstructor
    @Schema(description = "배정자 정보")
    public static class AssigneeInfo {

        @Schema(description = "사용자 ID", example = "36")
        private Long userId;

        @Schema(description = "사용자 username", example = "test1234")
        private String username;
    }
}
