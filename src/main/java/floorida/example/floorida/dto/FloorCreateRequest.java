package floorida.example.floorida.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Floor(세부 일정) 생성 요청")
public class FloorCreateRequest {
    
    @Schema(description = "연결할 일정(Schedule) ID", example = "1")
    @NotNull
    private Long scheduleId;
    
    @Schema(description = "세부 일정 제목", example = "추가 할 일")
    @NotBlank
    private String title;
    
    @Schema(description = "수행할 날짜", example = "2025-11-20")
    @NotNull
    private LocalDate scheduledDate;
}
