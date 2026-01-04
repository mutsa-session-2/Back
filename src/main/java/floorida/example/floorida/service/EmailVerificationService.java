package floorida.example.floorida.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;

@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_EXP_HOURS = 24;

    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.email.from:}")
    private String from;

    public EmailVerificationService(UserRepository userRepository, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
    }

    @Transactional
    public void sendVerification(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        IssuedVerification issued = issueVerificationToken(user);
        String link = buildVerificationLink(issued.token());
        String subject = "[Floorida] 이메일 인증을 완료해주세요";
        String body = "아래 링크를 눌러 이메일 인증을 완료해주세요.\n\n" + link + "\n\n" +
                "이 링크는 " + TOKEN_EXP_HOURS + "시간 동안 유효합니다.";

        // SimpleMailMessage에서 from은 설정하지 않아도 동작하지만, 명시 설정이 있으면 주입
        // (JavaMailSender 구현에 따라 from 기본값이 필요할 수 있음)
        if (from != null && !from.isBlank()) {
            body = body + "\n\n발신자: " + from;
        }

        emailSender.send(user.getEmail(), subject, body);
    }

    @Transactional
    public IssuedVerification issueVerificationToken(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        String token = generateToken();
        String tokenHash = sha256Hex(token);
        Instant expiresAt = Instant.now().plus(TOKEN_EXP_HOURS, ChronoUnit.HOURS);

        user.setEmailVerificationTokenHash(tokenHash);
        user.setEmailVerificationTokenExpiresAt(expiresAt);
        user.setEmailVerificationSentAt(Instant.now());
        userRepository.save(user);

        return new IssuedVerification(token, expiresAt);
    }

    public String buildVerificationLink(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        String normalizedBaseUrl = baseUrl;
        if (normalizedBaseUrl != null && normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl + "/api/auth/verify?token=" + token;
    }

    @Transactional
    public User verifyByTokenOrThrow(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        String hash = sha256Hex(token);
        User user = userRepository.findByEmailVerificationTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        Instant exp = user.getEmailVerificationTokenExpiresAt();
        if (exp != null && Instant.now().isAfter(exp)) {
            throw new IllegalArgumentException("Token expired");
        }

        user.setEmailVerified(Boolean.TRUE);
        user.setEmailVerifiedAt(Instant.now());
        user.setEmailVerificationTokenHash(null);
        user.setEmailVerificationTokenExpiresAt(null);

        return userRepository.save(user);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }

    public record IssuedVerification(String token, Instant expiresAt) {
    }
}
