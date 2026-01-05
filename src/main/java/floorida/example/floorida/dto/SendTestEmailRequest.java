package floorida.example.floorida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "테스트 이메일 발송 요청")
public class SendTestEmailRequest {

    @NotBlank
    @Email
    @Schema(description = "수신자 이메일", example = "someone@example.com")
    private String to;
}
