package floorida.example.floorida.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import floorida.example.floorida.dto.SendTestEmailRequest;
import floorida.example.floorida.service.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/health/email")
public class EmailTestController {

    private final Environment env;
    private final EmailSender emailSender;

    public EmailTestController(Environment env, EmailSender emailSender) {
        this.env = env;
        this.emailSender = emailSender;
    }

    @PostMapping("/send-test")
    @Operation(summary = "테스트 이메일 발송", description = "SMTP 설정이 실제로 동작하는지 테스트 메일을 발송합니다. X-Health-Token 헤더가 필요합니다.")
    public ResponseEntity<?> sendTest(
            @RequestHeader(value = "X-Health-Token", required = false) String token,
            @Valid @RequestBody SendTestEmailRequest req) {

        String expected = env.getProperty("app.health.token", "");
        if (expected == null || expected.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body("Health token not configured (set APP_HEALTH_TOKEN)");
        }
        if (token == null || token.isBlank() || !expected.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid X-Health-Token");
        }

        boolean enabled = Boolean.parseBoolean(env.getProperty("app.email.enabled", "true"));
        if (!enabled) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email sending is disabled (app.email.enabled=false)");
        }

        String subject = "[Floorida] SMTP 테스트 메일";
        String body = "이 메일은 Floorida SMTP 설정 테스트용입니다.";

        emailSender.send(req.getTo(), subject, body);
        return ResponseEntity.ok("sent");
    }
}
