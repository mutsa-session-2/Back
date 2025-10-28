# Floorida (Spring Boot + Security + JWT)

개인/팀 생산성 앱의 백엔드 API입니다. Spring Boot, Spring Security, JWT, JPA, MySQL, SpringDoc(OpenAPI) 기반입니다.

## 빠른 시작

1) 자바 21, Gradle 필요. 환경변수로 DB/JWT 세팅

- 프로젝트 루트에 `.env` 파일 생성(.env.example 참고)
```
DB_URL=jdbc:mysql://<host>:3306/<dbname>?useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
DB_USERNAME=<db_user>
DB_PASSWORD=<db_password>
JWT_SECRET=<base64_32bytes_secret>
```

2) 실행
```
./gradlew bootRun    # Windows: .\gradlew bootRun
```

3) 문서
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs

## 주요 엔드포인트

- POST /api/auth/register — 회원가입(email, password, username)
- POST /api/auth/login — 로그인, { accessToken }
- GET /api/me — 인증 필요(Authorization: Bearer <token>)

## 보안/비밀정보 관리

- 애플리케이션은 `.env`와 OS 환경변수를 통해 DB/JWT 값을 주입합니다
- `.env`는 `.gitignore`로 버전에 포함되지 않습니다
- 공개 저장소에 비밀값을 커밋하지 마세요

## 개발 메모

- DB 마이그레이션 도구는 아직 미포함(원하면 Flyway 추가 가능)
- 운영에서는 RDS SSL 검증 활성화를 권장(현재 개발 편의 옵션 포함)
