package floorida.example.floorida.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "개인 플레이스: 계획(일정)별 미달성 세부 일정(Floor) 목록")
public class UncompletedScheduleResponse {

    @Schema(description = "일정 ID", example = "1")
    private Long scheduleId;

    @Schema(description = "일정 제목", example = "토익 900점 달성")
    private String scheduleTitle;

    @Schema(description = "일정 색상", example = "#2E8B57")
    private String scheduleColor;

    @Schema(description = "일정 시작일", example = "2025-10-24", type = "string", format = "date")
    private LocalDate startDate;

    @Schema(description = "일정 종료일", example = "2025-10-31", type = "string", format = "date")
    private LocalDate endDate;

    @Schema(description = "미달성 세부 일정(Floor) 목록")
    private List<FloorDto> floors;

    @Getter
    @Builder
    @Schema(description = "미달성 세부 일정(Floor) 정보")
    public static class FloorDto {

        @Schema(description = "Floor ID", example = "10")
        private Long floorId;

        @Schema(description = "Floor 제목", example = "5일 차")
        private String title;

        @Schema(description = "예정 날짜", example = "2025-10-28", type = "string", format = "date")
        private LocalDate scheduledDate;
    }
}
