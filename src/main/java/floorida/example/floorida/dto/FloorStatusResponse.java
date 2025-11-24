package floorida.example.floorida.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Floor 완료 상태 응답")
public class FloorStatusResponse {
    
    @Schema(description = "Floor ID", example = "1")
    private Long floorId;
    
    @Schema(description = "일정 ID", example = "1")
    private Long scheduleId;
    
    @Schema(description = "일정 제목", example = "토익 900점 달성")
    private String scheduleTitle;
    
    @Schema(description = "일정 색상", example = "#2E8B57")
    private String scheduleColor;
    
    @Schema(description = "Floor 제목", example = "RC 문법 복습")
    private String floorTitle;
    
    @Schema(description = "예정 날짜", example = "2025-11-20")
    private LocalDate scheduledDate;
    
    @Schema(description = "완료 여부", example = "true")
    private boolean completed;
}
