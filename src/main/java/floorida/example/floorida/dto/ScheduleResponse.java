package floorida.example.floorida.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "일정 조회 응답")
public class ScheduleResponse {
    
    @Schema(
        description = "일정 고유 ID",
        example = "1",
        required = true
    )
    private Long scheduleId;
    
    @Schema(
        description = "일정 제목",
        example = "토익 900점 달성",
        required = true
    )
    private String title;
    
    @Schema(
        description = "시작일",
        example = "2025-10-24",
        required = true,
        type = "string",
        format = "date"
    )
    private LocalDate startDate;
    
    @Schema(
        description = "종료일",
        example = "2025-10-31",
        required = true,
        type = "string",
        format = "date"
    )
    private LocalDate endDate;
    
    @Schema(
        description = "일정 색상 (HEX 코드)",
        example = "#1E90FF",
        required = false
    )
    private String color;
    
    @Schema(
        description = "팀 ID (팀 일정인 경우만 값이 있음)",
        example = "null",
        required = false
    )
    private Long teamId;
    
    @Schema(
        description = "세부 계획 목록 (Floors)",
        required = true
    )
    private List<FloorDto> floors;

    @Getter
    @Builder
    @Schema(description = "세부 계획 (Floor) 정보")
    public static class FloorDto {
        
        @Schema(
            description = "Floor 고유 ID",
            example = "1",
            required = true
        )
        private Long floorId;
        
        @Schema(
            description = "세부 계획 제목",
            example = "RC 기본 문법 익히기",
            required = true
        )
        private String title;
        
        @Schema(
            description = "예정 날짜 (해당 Floor를 수행할 날짜)",
            example = "2025-10-24",
            required = false,
            type = "string",
            format = "date"
        )
        private LocalDate scheduledDate;
    }
}
