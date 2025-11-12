package floorida.example.floorida.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "수동 일정 생성 요청")
public class ScheduleCreateRequest {
    
    @NotBlank
    @Schema(
        description = "일정 제목",
        example = "토익 900점 달성",
        required = true,
        minLength = 1,
        maxLength = 255
    )
    private String title;

    @NotNull
    @Schema(
        description = "시작일 (YYYY-MM-DD 형식)",
        example = "2025-10-24",
        required = true,
        type = "string",
        format = "date"
    )
    private LocalDate startDate;

    @NotNull
    @Schema(
        description = "종료일 (YYYY-MM-DD 형식, startDate 이후여야 함)",
        example = "2025-10-31",
        required = true,
        type = "string",
        format = "date"
    )
    private LocalDate endDate;

    @Schema(
        description = "일정 색상 (HEX 코드, 미입력 시 자동 배정)",
        example = "#2E8B57",
        required = false,
        pattern = "^#[0-9A-Fa-f]{6}$"
    )
    private String color;

    @Schema(
        description = "팀 ID (팀 일정인 경우만 입력, 개인 일정은 null)",
        example = "null",
        required = false
    )
    private Long teamId;

    @Schema(
        description = "세부 계획(층) 목록 (선택 사항)",
        required = false
    )
    private List<FloorCreate> floors;

    @Getter
    @Setter
    @Schema(description = "세부 계획(층) 생성 정보")
    public static class FloorCreate {
        
        @NotBlank
        @Schema(
            description = "세부 계획 제목",
            example = "RC 파트 총정리",
            required = true,
            minLength = 1,
            maxLength = 255
        )
        private String title;
        
        @Schema(
            description = "예정 날짜 (YYYY-MM-DD 형식, 선택 사항)",
            example = "2025-10-24",
            required = false,
            type = "string",
            format = "date"
        )
        private LocalDate scheduledDate;
    }
}
