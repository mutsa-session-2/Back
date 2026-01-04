package floorida.example.floorida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "회원가입 응답 (이메일 인증 필요)")
public class RegisterResponse {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "user@floorida.local")
    private String email;

    @Schema(description = "메시지", example = "Verification email sent")
    private String message;
}
