package floorida.example.floorida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "API 에러 응답")
public class ApiErrorResponse {

    @Schema(description = "에러 코드", example = "EMAIL_NOT_VERIFIED")
    private String error;

    @Schema(description = "에러 메시지", example = "Email not verified")
    private String message;
}
