package floorida.example.floorida.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "일정 부분 수정 요청 (PATCH)")
public class ScheduleUpdateRequest {

    @Schema(description = "변경할 일정 제목", example = "자료구조 집중 학습 주간", required = false)
    private String title;

    @Schema(description = "변경할 시작일 (YYYY-MM-DD)", example = "2025-11-16", required = false, format = "date")
    private LocalDate startDate;

    @Schema(description = "변경할 종료일 (YYYY-MM-DD)", example = "2025-11-23", required = false, format = "date")
    private LocalDate endDate;

    @Schema(description = "변경할 목표 요약/설명", example = "집중 주간으로 핵심 자료구조 개념만 정리", required = false, maxLength = 2000)
    private String goalSummary;

    @Schema(description = "변경할 색상 (HEX)", example = "#FF6B6B", required = false, pattern = "^#[0-9A-Fa-f]{6}$")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    private String color;
}
