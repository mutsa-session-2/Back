package floorida.example.floorida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이메일(SMTP) 설정 헬스 체크 응답")
public class EmailHealthResponse {

    @Schema(description = "상태", example = "OK")
    private String status;

    @Schema(description = "이메일 발송 기능 활성화 여부", example = "true")
    private boolean emailEnabled;

    @Schema(description = "SMTP host 설정 여부", example = "true")
    private boolean smtpHostSet;

    @Schema(description = "SMTP username 설정 여부", example = "true")
    private boolean smtpUsernameSet;

    @Schema(description = "From 주소 설정 여부", example = "true")
    private boolean fromSet;

    @Schema(description = "베이스 URL 설정 여부", example = "true")
    private boolean baseUrlSet;

    @Schema(description = "추가 메시지")
    private String message;
}
