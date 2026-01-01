package floorida.example.floorida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Floor(세부 일정) 수정 요청")
public class FloorUpdateRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    @Schema(
        description = "Floor 제목(할 일 내용)",
        example = "RC 파트 총정리",
        required = true,
        minLength = 1,
        maxLength = 255
    )
    private String title;
}
