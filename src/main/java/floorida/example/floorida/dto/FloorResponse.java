package floorida.example.floorida.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "오늘 할 일 응답")
public class FloorResponse {
    
    @Schema(description = "Floor 고유 ID", example = "1")
    private Long floorId;
    
    @Schema(description = "소속 일정 ID", example = "1")
    private Long scheduleId;
    
    @Schema(description = "소속 일정 제목", example = "토익 900점 달성")
    private String scheduleTitle;
    
    @Schema(description = "소속 일정 색상", example = "#FF6B6B")
    private String scheduleColor;
    
    @Schema(description = "할 일 제목", example = "RC 파트 총정리")
    private String floorTitle;
    
    @Schema(description = "예정 날짜", example = "2025-11-13")
    private LocalDate scheduledDate;
}
