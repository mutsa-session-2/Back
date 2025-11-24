package floorida.example.floorida.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "날짜별 완료 통계 응답")
public class DailyCompletionResponse {
    
    @Schema(description = "날짜", example = "2025-11-20")
    private LocalDate date;
    
    @Schema(description = "전체 Floor 개수", example = "5")
    private int totalFloors;
    
    @Schema(description = "완료된 Floor 개수", example = "3")
    private int completedFloors;
    
    @Schema(description = "완료율 (0~100)", example = "60")
    private int completionRate;
    
    @Schema(description = "해당 날짜의 Floor 목록")
    private List<FloorStatusResponse> floors;
}
