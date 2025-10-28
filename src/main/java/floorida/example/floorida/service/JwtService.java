package floorida.example.floorida.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import floorida.example.floorida.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(JwtProperties props) {
        // 시크릿이 Base64일 수도 평문일 수도 있으므로 안전하게 처리하고,
        // 항상 256bit 키로 파생하여 HMAC-SHA256 요구사항을 만족시킨다.
        byte[] rawSecretBytes;
        try {
            rawSecretBytes = Base64.getDecoder().decode(props.getSecret());
        } catch (IllegalArgumentException e) {
            rawSecretBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        }

        byte[] keyMaterial;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            keyMaterial = sha256.digest(rawSecretBytes); // 32바이트
        } catch (Exception e) {
            byte[] utf8 = props.getSecret().getBytes(StandardCharsets.UTF_8);
            keyMaterial = new byte[32];
            for (int i = 0; i < 32; i++) {
                keyMaterial[i] = utf8[i % utf8.length];
            }
        }

        this.key = Keys.hmacShaKeyFor(keyMaterial);
        this.expirationMillis = props.getExpiration();
    }

    public String generateToken(String subjectEmail) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subjectEmail)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
