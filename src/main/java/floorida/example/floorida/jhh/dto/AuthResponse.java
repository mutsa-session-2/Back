package floorida.example.floorida.jhh.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken; // JWT 토큰 (필수)
    private Long userId;        // 다음 API 호출이나 로컬 스토리지 저장용 
    private String email;       // 화면에 "환영합니다 OO님" 표시용 
}