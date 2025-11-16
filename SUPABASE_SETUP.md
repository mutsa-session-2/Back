# 🚀 Supabase 마이그레이션 가이드

## 1. Supabase 프로젝트 생성

1. https://supabase.com 접속 후 로그인
2. **New Project** 클릭
3. 프로젝트 설정:
   - **Name**: `floorida` (또는 원하는 이름)
   - **Database Password**: 강력한 비밀번호 입력 (**꼭 저장!**)
   - **Region**: **Singapore** (동남아시아) 또는 **Tokyo** (일본)
   - **Pricing Plan**: **Free** (500MB DB + 2GB 전송)

## 2. 연결 정보 확인

1. Supabase 대시보드 → **Settings** (톱니바퀴 아이콘)
2. **Database** 메뉴 클릭
3. **Connection string** 섹션에서 **URI** 복사

예시:
```
postgresql://postgres.xxxxxxxxxxxxx:[YOUR-PASSWORD]@aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres
```

## 3. .env 파일 설정

`floorida/.env` 파일을 다음과 같이 수정:

```properties
# ===============================
# Supabase PostgreSQL 연결
# ===============================
DB_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_password_here

# ===============================
# JWT 설정
# ===============================
JWT_SECRET=your_super_secret_jwt_key_min_256_bits

# ===============================
# AWS S3 (또는 Supabase Storage)
# ===============================
AWS_S3_ACCESS_KEY=your_key
AWS_S3_SECRET_KEY=your_secret
AWS_S3_BUCKET_NAME=your_bucket
AWS_S3_REGION=ap-southeast-1

# ===============================
# OpenAI API
# ===============================
OPENAI_API_KEY=sk-your-openai-api-key
```

### 📝 DB_URL 형식 변환

Supabase에서 제공하는 URL을 Spring Boot용 JDBC URL로 변환:

**Before (Supabase 기본):**
```
postgresql://postgres.abc123:[PASSWORD]@aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres
```

**After (Spring Boot용):**
```
jdbc:postgresql://db.abc123.supabase.co:5432/postgres?sslmode=require
```

변환 규칙:
- `postgresql://` → `jdbc:postgresql://`
- `postgres.abc123:[PASSWORD]@aws-0-ap-southeast-1.pooler` → `db.abc123`
- 끝에 `?sslmode=require` 추가 (SSL 필수)

## 4. 기존 MySQL 데이터 마이그레이션 (선택)

### Option A: 자동 스키마 생성 (권장)

1. `.env` 파일 수정 완료
2. `application.properties`에서 `spring.jpa.hibernate.ddl-auto=update` 확인
3. 서버 재시작 → JPA가 자동으로 테이블 생성

**장점**: 간단, 빠름  
**단점**: 기존 데이터는 없어짐

### Option B: 수동 SQL 실행

1. Supabase 대시보드 → **SQL Editor**
2. 아래 SQL 실행:

```sql
-- users 테이블
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- user_profiles 테이블
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    planning_tendency VARCHAR(50),
    daily_study_hours VARCHAR(50),
    points INT NOT NULL DEFAULT 0,
    personal_level INT NOT NULL DEFAULT 1
);

-- schedules 테이블
CREATE TABLE schedules (
    schedule_id BIGSERIAL PRIMARY KEY,
    creator_user_id BIGINT NOT NULL REFERENCES users(user_id),
    team_id BIGINT,
    title VARCHAR(255) NOT NULL,
    original_goal VARCHAR(1000),
    goal_summary VARCHAR(2000),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- floors 테이블
CREATE TABLE floors (
    floor_id BIGSERIAL PRIMARY KEY,
    schedule_id BIGINT NOT NULL REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    creator_user_id BIGINT NOT NULL REFERENCES users(user_id),
    title VARCHAR(255) NOT NULL,
    scheduled_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- floor_statuses 테이블
CREATE TABLE floor_statuses (
    status_id BIGSERIAL PRIMARY KEY,
    floor_id BIGINT NOT NULL REFERENCES floors(floor_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE(floor_id, user_id)
);

-- characters 테이블
CREATE TABLE characters (
    character_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    equipped_items JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_schedules_creator ON schedules(creator_user_id);
CREATE INDEX idx_floors_schedule ON floors(schedule_id);
CREATE INDEX idx_floors_creator_date ON floors(creator_user_id, scheduled_date);
CREATE INDEX idx_floor_statuses_floor_user ON floor_statuses(floor_id, user_id);
```

## 5. 서버 재시작 및 테스트

```bash
# Gradle 빌드 (의존성 업데이트)
./gradlew clean build

# 서버 실행
./gradlew bootRun

# 또는 JAR 실행
java -jar build/libs/app.jar
```

### 테스트 체크리스트

- [ ] 서버 정상 시작 (로그 확인)
- [ ] 회원가입 API 테스트
- [ ] 로그인 API 테스트
- [ ] 일정 생성 테스트
- [ ] Floor 완료 + 코인 적립 테스트
- [ ] 온보딩 API 테스트

## 6. Supabase 추가 기능 활용 (선택)

### 6-1. Supabase Auth 활용

JWT를 직접 관리하는 대신 Supabase Auth 사용 가능:
- 소셜 로그인 (Google, GitHub 등)
- 이메일 인증
- 비밀번호 재설정

### 6-2. Supabase Storage

AWS S3 대신 Supabase Storage 사용:
- 무료 1GB 저장
- CDN 자동 제공
- Public/Private 버킷 지원

## 7. 비용 비교

### Lightsail (기존)
- MySQL DB: **최소 $15/월**
- Public IP: 추가 비용 발생 가능
- 백업: 수동 관리

### Supabase (신규)
- **Free Tier**: $0/월
  - 500MB DB
  - 2GB 전송/월
  - 50MB 파일 스토리지
  - 자동 백업 7일
- **Pro Tier**: $25/월
  - 8GB DB
  - 50GB 전송/월
  - 100GB 파일 스토리지
  - 자동 백업 30일

## 8. 보안 설정

Supabase는 기본적으로 안전하지만, 추가 설정 권장:

1. **Row Level Security (RLS) 활성화**
   ```sql
   ALTER TABLE users ENABLE ROW LEVEL SECURITY;
   ALTER TABLE schedules ENABLE ROW LEVEL SECURITY;
   -- 각 테이블별로 정책 설정
   ```

2. **IP 화이트리스트** (Pro 플랜 이상)
   - 특정 IP만 DB 접근 허용

3. **SSL 필수**
   - 이미 설정됨 (`?sslmode=require`)

## 9. 문제 해결

### "Connection refused" 에러
- Supabase 프로젝트가 일시 중지(Paused)되었을 수 있음
- 대시보드에서 **Resume** 클릭

### "SSL required" 에러
- DB_URL에 `?sslmode=require` 추가 확인

### "Authentication failed" 에러
- DB_PASSWORD 다시 확인
- Supabase 대시보드에서 비밀번호 재설정

## 📞 도움

문제가 생기면:
1. Supabase 대시보드 → **Logs** 확인
2. Spring Boot 로그 확인 (`logging.level.org.springframework=DEBUG`)
3. Supabase 공식 문서: https://supabase.com/docs

