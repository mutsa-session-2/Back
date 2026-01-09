package floorida.example.floorida.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "날짜별 완료율 응답 (주간 캘린더용)")
public class WeeklyCompletionRateResponse {
    
    @Schema(description = "날짜", example = "2026-01-09")
    private LocalDate date;
    
    @Schema(description = "전체 Floor 개수", example = "5")
    private int totalFloors;
    
    @Schema(description = "완료된 Floor 개수", example = "3")
    private int completedFloors;
    
    @Schema(description = "완료율 (0~100)", example = "60")
    private int completionRate;
}
