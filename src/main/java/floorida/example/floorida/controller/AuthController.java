package floorida.example.floorida.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import floorida.example.floorida.dto.ApiErrorResponse;
import floorida.example.floorida.dto.AuthResponse;
import floorida.example.floorida.dto.RegisterResponse;
import floorida.example.floorida.dto.LoginRequest;
import floorida.example.floorida.dto.SignupRequest;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.service.EmailVerificationService;
import floorida.example.floorida.service.JwtService;
import floorida.example.floorida.service.UserService;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Auth", description = "회원가입 및 로그인입니다")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(UserService userService, JwtService jwtService, EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "회원가입 후 이메일 인증 메일을 발송합니다. (인증 전에는 로그인 불가)")
    @ApiResponse(responseCode = "201", description = "성공 시 유저 ID 반환",
        content = @Content(schema = @Schema(implementation = RegisterResponse.class)))
    @ApiResponse(responseCode = "400", description = "요청 오류(중복 이메일/유저명 등)",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        try {
            // 1. 회원가입 진행 (DB 저장)
            User user = userService.register(request);

            // 2. 이메일 인증 메일 발송
            emailVerificationService.sendVerification(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(user.getUserId(), user.getEmail(), "Verification email sent"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse("BAD_REQUEST", e.getMessage()));
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "이메일 인증", description = "회원가입 시 발송된 링크(token)로 이메일 인증을 완료합니다.")
    @ApiResponse(responseCode = "200", description = "인증 성공")
    @ApiResponse(responseCode = "400", description = "토큰 오류/만료",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<?> verify(
            @RequestParam("token") String token,
            @RequestHeader(value = "Accept", required = false) String accept) {
        try {
            emailVerificationService.verifyByTokenOrThrow(token);
            if (accept != null && accept.contains(MediaType.TEXT_HTML_VALUE)) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(htmlPage("이메일 인증 완료", "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다."));
            }
            return ResponseEntity.ok("Email verified");
        } catch (IllegalArgumentException e) {
            if (accept != null && accept.contains(MediaType.TEXT_HTML_VALUE)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.TEXT_HTML)
                        .body(htmlPage("이메일 인증 실패", "인증 링크가 유효하지 않거나 만료되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse("INVALID_TOKEN", e.getMessage()));
        }
    }

    private String htmlPage(String title, String message) {
        return "<!doctype html>" +
                "<html lang=\"ko\"><head>" +
                "<meta charset=\"utf-8\"/>" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>" +
                "<title>" + escapeHtml(title) + "</title>" +
                "<style>body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;margin:24px;}" +
                ".box{max-width:560px;padding:16px;border:1px solid #ddd;border-radius:12px;}" +
                "</style></head><body>" +
                "<div class=\"box\">" +
                "<h1>" + escapeHtml(title) + "</h1>" +
                "<p>" + escapeHtml(message) + "</p>" +
                "</div></body></html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = """
            이메일/비밀번호로 로그인하고 JWT를 발급합니다.

            - 코인 정책
              - (추가) 출석(접속일) 기준 하루 1회 **10코인** 지급
              - (첫 로그인) 회원가입 직후 첫 로그인 시 **50코인** 지급
                - 첫 로그인 날에도 출석 보상 10코인은 함께 지급됩니다.
            """
    )
    @ApiResponse(responseCode = "200", description = "JWT 액세스 토큰 반환",
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "로그인 실패(아이디/비밀번호 불일치)",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "이메일 미인증",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserService.LoginResult result = userService.authenticateOrThrow(request);
            User user = result.user();
            String token = jwtService.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(
                token, 
                user.getUserId(), 
                user.getEmail(), 
                result.dailyRewardGiven(), 
                result.firstLoginBonusGiven()
            ));
        } catch (IllegalArgumentException e) {
            if ("Email not verified".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiErrorResponse("EMAIL_NOT_VERIFIED", "Email not verified"));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiErrorResponse("INVALID_CREDENTIALS", "Invalid credentials"));
        }
    }
}
