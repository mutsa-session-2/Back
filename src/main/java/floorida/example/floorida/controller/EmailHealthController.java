package floorida.example.floorida.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import floorida.example.floorida.dto.EmailHealthResponse;

@RestController
@RequestMapping("/api/health")
public class EmailHealthController {

    private final Environment env;

    public EmailHealthController(Environment env) {
        this.env = env;
    }

    @GetMapping("/email")
    public ResponseEntity<EmailHealthResponse> emailHealth() {
        boolean enabled = Boolean.parseBoolean(env.getProperty("app.email.enabled", "true"));
        String smtpHost = env.getProperty("spring.mail.host");
        String smtpUser = env.getProperty("spring.mail.username");
        String from = env.getProperty("app.email.from");
        String baseUrl = env.getProperty("app.base-url");

        boolean smtpHostSet = smtpHost != null && !smtpHost.isBlank();
        boolean smtpUserSet = smtpUser != null && !smtpUser.isBlank();
        boolean fromSet = from != null && !from.isBlank();
        boolean baseUrlSet = baseUrl != null && !baseUrl.isBlank();

        String status = (enabled && smtpHostSet && smtpUserSet && fromSet && baseUrlSet) ? "OK" : "WARN";
        String message = status.equals("OK")
                ? "SMTP 설정이 유효해 보입니다"
                : "SMTP/From/BaseUrl/app.email.enabled 설정을 확인하세요";

        return ResponseEntity.ok(EmailHealthResponse.builder()
                .status(status)
                .emailEnabled(enabled)
                .smtpHostSet(smtpHostSet)
                .smtpUsernameSet(smtpUserSet)
                .fromSet(fromSet)
                .baseUrlSet(baseUrlSet)
                .message(message)
                .build());
    }

    @org.springframework.web.bind.annotation.PostMapping("/check-lock")
    public ResponseEntity<Boolean> checkLock(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body) {
        String input = body.get("password");
        // Environment variable: APP_HEALTH_PASSWORD
        String expected = env.getProperty("APP_HEALTH_PASSWORD");
        if (expected == null || expected.isBlank()) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(expected.equals(input));
    }
}
