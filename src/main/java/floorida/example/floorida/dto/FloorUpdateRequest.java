package floorida.example.floorida.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Floor(세부 일정) 수정 요청")
public class FloorUpdateRequest {

    @Size(min = 1, max = 255)
    @Schema(
        description = "Floor 제목(할 일 내용)",
        example = "RC 파트 총정리",
        required = false,
        minLength = 1,
        maxLength = 255
    )
    private String title;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(
        description = "Floor 날짜 (YYYY-MM-DD). 제공 시 해당 날짜로 이동",
        example = "2025-11-13",
        required = false
    )
    private LocalDate scheduledDate;

    @AssertTrue(message = "title 또는 scheduledDate 중 하나는 반드시 제공되어야 합니다")
    @Schema(hidden = true)
    public boolean isAnyFieldProvided() {
        return (title != null && !title.isBlank()) || scheduledDate != null;
    }
}
