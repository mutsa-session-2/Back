package floorida.example.floorida.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Floorida API")
                        .description("""
                            🏢 **Floorida - 목표 달성 게이미피케이션 플랫폼**
                            
                            일정 관리와 게이미피케이션을 결합한 생산성 향상 플랫폼입니다.
                            
                            ## 주요 기능
                            
                            ### 🎯 일정 관리 (Schedule)
                            - **수동 생성**: 사용자가 직접 목표와 세부 계획 작성
                            - **AI 자동 생성**: OpenAI를 활용한 목표 기반 자동 계획 수립
                            - 개인/팀 일정 지원
                            - 날짜별 세부 계획(Floor) 관리
                            
                            ### 🏗️ 층(Floor) 시스템
                            - 각 일정은 여러 개의 층(세부 계획)으로 구성
                            - 층 완료 시 경험치 획득 및 레벨업
                            - 오늘의 모든 층 완료 시 개인/팀 레벨 +1
                            
                            ### 🎮 게이미피케이션
                            - 개인 레벨 시스템 (personal_level)
                            - 팀 레벨 시스템 (team level)
                            - 배지 획득 시스템
                            - 캐릭터 꾸미기 (아이템 상점)
                            
                            ### 👥 팀 협업
                            - 팀 생성 및 초대 코드 공유
                            - 팀 일정 공유 및 함께 달성
                            - 팀 배지 및 레벨 공유
                            
                            ## 인증
                            
                            대부분의 API는 **JWT 인증**이 필요합니다.
                            
                            **사용 방법:**
                            1. `/api/auth/register`로 회원가입
                            2. `/api/auth/login`으로 로그인하여 JWT 토큰 발급
                            3. 이후 모든 요청에 `Authorization: Bearer {token}` 헤더 포함
                            
                            ## API 카테고리
                            
                            - **인증 (Auth)**: 회원가입, 로그인
                            - **일정 관리 (Schedule)**: 일정 생성(수동/AI), 조회
                            - **내 정보 (Me)**: 현재 사용자 정보 조회
                            
                            ## 기술 스택
                            
                            - **Backend**: Spring Boot 3.5.7, Java 17
                            - **Database**: MySQL 8.0 (AWS RDS)
                            - **AI**: OpenAI GPT-4o-mini
                            - **Security**: JWT (Bearer Token)
                            - **API Docs**: Swagger/OpenAPI 3.0
                            
                            ## 날짜 형식
                            
                            모든 날짜는 **ISO 8601** 형식을 사용합니다: `YYYY-MM-DD`
                            
                            예시: `2025-10-24`
                            
                            ## 에러 응답
                            
                            모든 에러는 다음 형식으로 반환됩니다:
                            ```json
                            {
                              "error": "ErrorType",
                              "message": "상세 에러 메시지"
                            }
                            ```
                            
                            ## 지원
                            
                            - **GitHub**: mutsa-session-2/Back
                            - **배포 URL**: https://app.floorida.site
                            """)
                        .version("v1.0.0")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Floorida Team")
                                .url("https://app.floorida.site")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                    JWT 인증 토큰을 입력하세요.
                                    
                                    **토큰 획득 방법:**
                                    1. `/api/auth/login` 엔드포인트로 로그인
                                    2. 응답에서 `token` 값 복사
                                    3. 여기에 토큰 값만 입력 (Bearer 접두사 제외)
                                    
                                    **예시:**
                                    - ✅ 올바른 입력: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                                    - ❌ 잘못된 입력: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                                    """)));
    }
}
