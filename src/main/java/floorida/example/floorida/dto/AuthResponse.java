package floorida.example.floorida.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String accessToken; // JWT 토큰 (필수)
    private Long userId;        // 다음 API 호출이나 로컬 스토리지 저장용 
    private String email;       // 화면에 "환영합니다 OO님" 표시용 
    
    private boolean dailyRewardGiven;     // 오늘 출석 보상(10코인) 지급 여부 (이미 받았으면 false)
    private boolean firstLoginBonusGiven; // 첫 로그인(회원가입) 보상(50코인) 지급 여부

}