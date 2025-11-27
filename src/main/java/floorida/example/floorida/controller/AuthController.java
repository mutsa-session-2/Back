package floorida.example.floorida.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import floorida.example.floorida.dto.AuthResponse;
import floorida.example.floorida.dto.LoginRequest;
import floorida.example.floorida.dto.SignupRequest;
import floorida.example.floorida.entity.User;
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

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "회원가입 후 자동 로그인 (토큰 및 유저정보 반환)")
    @ApiResponse(responseCode = "201", description = "성공 시 토큰과 유저 ID 반환",
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        try {
            // 1. 회원가입 진행 (DB 저장)
            User user = userService.register(request);
            
            // 2. 토큰 생성 (자동 로그인 효과)
            String token = jwtService.generateToken(user.getEmail());
            
            // 3. DTO 생성 (토큰 + 유저 ID + 이메일)
            AuthResponse response = new AuthResponse(token, user.getUserId(), user.getEmail());
            
            // 4. 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 JWT 발급")
    @ApiResponse(responseCode = "200", description = "JWT 액세스 토큰 반환",
        content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateOrThrow(request);
            String token = jwtService.generateToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getUserId(), user.getEmail()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
