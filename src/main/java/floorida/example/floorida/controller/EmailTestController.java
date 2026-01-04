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
import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.service.EmailSender;
import floorida.example.floorida.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/health/email")
public class EmailTestController {

    private final Environment env;
    private final EmailSender emailSender;
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    public EmailTestController(
            Environment env,
            EmailSender emailSender,
            UserRepository userRepository,
            EmailVerificationService emailVerificationService
    ) {
        this.env = env;
        this.emailSender = emailSender;
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/issue-verification")
    @Operation(summary = "이메일 인증 링크 발급(메일 발송 없음)", description = "가입된 유저 이메일로 인증 토큰을 발급하고(저장) 인증 링크를 반환합니다. X-Health-Token 헤더가 필요합니다.")
    public ResponseEntity<?> issueVerification(
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

        User user = userRepository.findByEmail(req.getTo()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found for email");
        }

        EmailVerificationService.IssuedVerification issued = emailVerificationService.issueVerificationToken(user);
        String link = emailVerificationService.buildVerificationLink(issued.token());
        return ResponseEntity.ok(new IssueVerificationResponse(issued.token(), link, issued.expiresAt().toString()));
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

    private record IssueVerificationResponse(String token, String link, String expiresAt) {
    }
}
