# Postman 테스트 가이드

## 기본 설정

### 1. 환경 변수 설정
Postman에서 다음 변수들을 설정하세요:

```
BASE_URL: http://localhost:8080
JWT_TOKEN: (로그인 후 받은 토큰)
```

---

## API 테스트 순서

### 1️⃣ 회원가입 (POST /api/auth/register)

**URL:** `{{BASE_URL}}/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "test@example.com",
  "username": "testuser",
  "password": "Test1234!"
}
```

**예상 응답 (201):**
```json
{
  "userId": 1,
  "email": "test@example.com",
  "message": "Verification email sent"
}
```

**다음 단계:** 메일함에서 인증 링크를 클릭하거나, 링크의 `token`으로 `GET /api/auth/verify`를 호출하세요.

---

### 2️⃣ 이메일 인증 (GET /api/auth/verify?token=...)

**URL:** `{{BASE_URL}}/api/auth/verify?token={{TOKEN}}`

**예상 응답 (200):**
```
Email verified
```

**예상 응답 (400) - 토큰 오류/만료:**
```json
{
  "error": "INVALID_TOKEN",
  "message": "Invalid token"
}
```

---

### 3️⃣ 로그인 (POST /api/auth/login)

**URL:** `{{BASE_URL}}/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "test@example.com",
  "password": "Test1234!"
}
```

**예상 응답 (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "test@example.com"
}
```

**예상 응답 (403) - 이메일 미인증:**
```json
{
  "error": "EMAIL_NOT_VERIFIED",
  "message": "Email not verified"
}
```

**⚠️ 중요:** 응답의 `accessToken` 값을 복사해서 환경 변수 `JWT_TOKEN`에 저장하세요!

---

### ✅ 내 뱃지 + 연속 출석 요약 조회 (GET /api/me/badges/summary)

뱃지 팝업에서 "몇 일 연속 출석" 정보를 표시하기 위해,
뱃지 목록과 함께 `attendStreak`(일일 접속 보상 기준 연속 출석 일수)를 반환합니다.

**URL:** `{{BASE_URL}}/api/me/badges/summary`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (200):**
```json
{
  "attendStreak": 3,
  "asOfDate": "2026-01-06",
  "badges": [
    {
      "badgeId": 1,
      "name": "1일출석",
      "type": "ATTENDANCE",
      "description": "연속 1일 출석 달성",
      "imageUrl": "https://...",
      "earnedAt": "2026-01-04T12:34:56Z"
    }
  ]
}
```

**참고:**
- `attendStreak`는 로그인 시 지급되는 "일일 접속 보상" 기준으로 계산됩니다.
- 아직 오늘 접속 보상을 받지 않았어도, 어제가 마지막 접속일이면(오늘 접속 전) 기존 streak 값을 그대로 반환합니다.

**Postman에서 streak 테스트하는 방법**
- (Day 1) 로그인 직후 `GET /api/me/badges/summary`를 호출하면 보통 `attendStreak = 1`이 됩니다.
  - 이유: "연속 1일"은 **첫 날 접속 보상을 받은 시점**을 1일로 카운트합니다.
- (Day 2) 실제로는 **다음 날** 다시 로그인해야 `attendStreak = 2`가 됩니다.
  - 로컬에서 날짜를 하루 넘기기 어렵다면 DB 값을 임시로 조정해서 시뮬레이션할 수 있습니다.

예) Supabase(PostgreSQL)에서 어제로 돌려놓기 (userId가 1인 경우)
```sql
-- 1) 마지막 접속 보상일을 어제로 변경
UPDATE user_profiles
SET last_daily_login_reward_date = CURRENT_DATE - INTERVAL '1 day'
WHERE user_id = 1;

-- 2) streak를 1로 맞춰둔 뒤
UPDATE user_profiles
SET daily_login_streak = 1
WHERE user_id = 1;
```

그 다음 Postman에서:
1) `POST /api/auth/login` 다시 호출
2) `GET /api/me/badges/summary` 호출 → `attendStreak`가 2로 증가하는지 확인

---

### 3️⃣ 수동 일정 생성 (POST /api/schedules)

**URL:** `{{BASE_URL}}/api/schedules`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN}}
```

**Body (JSON):**
```json
{
  "title": "토익 900점 달성",
  "startDate": "2025-11-16",
  "endDate": "2025-12-07",
  "color": "#2E8B57",
  "teamId": null
}
```

**예상 응답 (200):**
```json
{
  "scheduleId": 1,
  "title": "토익 900점 달성",
  "originalGoal": "3주 안에 토익 900점 달성하기",
  "goalSummary": "RC와 LC를 균형있게 학습하며 실전 모의고사 중심으로 준비",
  "startDate": "2025-11-16",
  "endDate": "2025-12-07",
  "color": "#2E8B57",
  "teamId": null,
  "floors": [
    {
      "floorId": 1,
      "title": "RC 파트 기본 문법 복습",
      "scheduledDate": "2025-11-16"
    },
    {
      "floorId": 2,
      "title": "LC Part 1-2 집중 훈련",
      "scheduledDate": "2025-11-17"
    },
    {
      "floorId": 3,
      "title": "실전 모의고사 1회",
      "scheduledDate": "2025-11-20"
    }
  ]
}
```

---

### 4️⃣ AI 일정 생성 (POST /api/schedules/ai)

**URL:** `{{BASE_URL}}/api/schedules/ai`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN}}
```

**Body (JSON) - 새 필드 포함:**
```json
{
  "goal": "한 주 만에 자료구조 기본기 다지기",
  "title": "자료구조 집중 학습 주간",
  "startDate": "2025-11-16",
  "endDate": "2025-11-23",
  "teamId": null,
  "color": "#1E90FF"
}
```

**예상 응답 (200):**
```json
{
  "scheduleId": 2,
  "title": "자료구조 집중 학습 주간",
  "originalGoal": "한 주 만에 자료구조 기본기 다지기",
  "goalSummary": "목표: 한 주 만에 자료구조 기본기 다지기 | 기간: 2025-11-16~2025-11-23 | 단계 수: 8",
  "startDate": "2025-11-16",
  "endDate": "2025-11-23",
  "color": "#1E90FF",
  "teamId": null,
  "floors": [
    {
      "floorId": 4,
      "title": "배열과 연결 리스트 개념 이해",
      "scheduledDate": "2025-11-16"
    },
    {
      "floorId": 5,
      "title": "스택과 큐 구현 연습",
      "scheduledDate": "2025-11-17"
    }
    // ... AI가 생성한 나머지 floors
  ]

---

## 삭제 API

### ✅ Floor 단건 삭제 (DELETE /api/floors/{floorId})

**URL:** `{{BASE_URL}}/api/floors/{{FLOOR_ID}}`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (204):**
```
(no content)
```

**설명:**
- 해당 Floor(세부 일정)만 삭제합니다.
- 완료 기록(FloorStatus)이 있어도 함께 정리되어 500이 발생하지 않습니다.

---

### ✅ 일정의 floors 일괄 삭제 (DELETE /api/floors/schedule/{scheduleId})

**URL:** `{{BASE_URL}}/api/floors/schedule/{{SCHEDULE_ID}}`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (204):**
```
(no content)
```

**설명:**
- 일정(Schedule) 자체를 삭제합니다. (일정이 삭제되면 포함된 floors도 같이 삭제됩니다)
- 완료 기록(FloorStatus)도 함께 정리됩니다.

---

### ✅ 일정 삭제 (DELETE /api/schedules/{id})

**URL:** `{{BASE_URL}}/api/schedules/{{SCHEDULE_ID}}`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (204):**
```
(no content)
```

**설명:**
- 일정을 삭제합니다. 포함된 floors와 완료 기록도 함께 삭제됩니다.


**💡 참고:** `title`을 생략하면 `goal` 값이 그대로 제목으로 사용됩니다.

---

### ✅ 내 일정 목록 조회 (GET /api/schedules)

**URL:** `{{BASE_URL}}/api/schedules`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (200):**
```json
[
  {
    "scheduleId": 1,
    "title": "토익 900점 달성",
    "originalGoal": "토익 900점 달성",
    "goalSummary": null,
    "startDate": "2025-11-16",
    "endDate": "2025-12-07",
    "color": "#2E8B57",
    "teamId": null,
    "floors": [
      {
        "floorId": 1,
        "title": "1일 차",
        "scheduledDate": "2025-11-16"
      }
    ]
  }
]
```

---

### 5️⃣ 일정 조회 (GET /api/schedules/{id})

**URL:** `{{BASE_URL}}/api/schedules/1`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (200):**
```json
{
  "scheduleId": 1,
  "title": "토익 900점 달성",
  "originalGoal": "3주 안에 토익 900점 달성하기",
  "goalSummary": "RC와 LC를 균형있게 학습하며 실전 모의고사 중심으로 준비",
  "startDate": "2025-11-16",
  "endDate": "2025-12-07",
  "color": "#2E8B57",
  "teamId": null,
  "floors": [...]
}
```

---

### 6️⃣ 일정 부분 수정 (PATCH /api/schedules/{id}) ✨ 신규

**URL:** `{{BASE_URL}}/api/schedules/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN}}
```

**Body (JSON) - 예시 1: 제목만 변경**
```json
{
  "title": "토익 950점으로 목표 상향!"
}
```

**Body (JSON) - 예시 2: 기간 연장**
```json
{
  "endDate": "2025-12-31"
}
```

**Body (JSON) - 예시 3: 전체 수정**
```json
{
  "title": "Spring Boot 심화 학습",
  "startDate": "2025-11-20",
  "endDate": "2025-12-20",
  "goalSummary": "JPA, Security, Batch를 한 달간 집중 학습",
  "color": "#4CAF50"
}
```

**예상 응답 (200):**
```json
{
  "scheduleId": 1,
  "title": "토익 950점으로 목표 상향!",
  "originalGoal": "3주 안에 토익 900점 달성하기",
  "goalSummary": "RC와 LC를 균형있게 학습하며 실전 모의고사 중심으로 준비",
  "startDate": "2025-11-16",
  "endDate": "2025-12-07",
  "color": "#2E8B57",
  "teamId": null,
  "floors": [...]
}
```

**⚠️ 주의:**
- `originalGoal`은 수정할 수 없습니다 (AI 생성 시 입력한 원본 유지)
- 제공하지 않은 필드는 변경되지 않습니다
- 날짜 범위 검증: `startDate ≤ endDate`

---

### 7️⃣ 일정 삭제 (DELETE /api/schedules/{id}) ✨ 신규

**URL:** `{{BASE_URL}}/api/schedules/1`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (204 No Content):**
- 응답 본문 없음
- HTTP 상태 코드 204

**⚠️ 주의:**
- 삭제된 일정은 복구할 수 없습니다
- 연관된 모든 Floors도 함께 삭제됩니다
- 본인이 생성한 일정만 삭제 가능

---

### 8️⃣ 오늘 할 일 조회 (GET /api/floors/today)

**URL:** `{{BASE_URL}}/api/floors/today`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (200):**
```json
[
  {
    "floorId": 1,
    "floorTitle": "RC 파트 기본 문법 복습",
    "scheduledDate": "2025-11-15",
    "scheduleId": 1,
    "scheduleTitle": "토익 900점 달성",
    "scheduleColor": "#2E8B57"
  },
  {
    "floorId": 4,
    "floorTitle": "배열과 연결 리스트 개념 이해",
    "scheduledDate": "2025-11-15",
    "scheduleId": 2,
    "scheduleTitle": "자료구조 집중 학습 주간",
    "scheduleColor": "#1E90FF"
  }
]
```

---

### 9️⃣ 특정 날짜 할 일 조회 (GET /api/floors/date/{date})

**URL:** `{{BASE_URL}}/api/floors/date/2025-11-20`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (200):**
```json
[
  {
    "floorId": 3,
    "floorTitle": "실전 모의고사 1회",
    "scheduledDate": "2025-11-20",
    "scheduleId": 1,
    "scheduleTitle": "토익 900점 달성",
    "scheduleColor": "#2E8B57"
  }
]
```

---

### 🔟 내 캐릭터 조회 (GET /api/characters/me)

**URL:** `{{BASE_URL}}/api/characters/me`

**Headers:**
```
Authorization: Bearer {{JWT_TOKEN}}
```

**예상 응답 (200):**
```json
{
  "characterId": 1,
  "imageUrl": "https://floorida-bucket.s3.us-east-1.amazonaws.com/characters/default-character.png"
}
```

---

## 오류 응답 예시

### 401 Unauthorized (토큰 없음/만료)
```json
{
  "error": "Unauthorized",
  "message": "유효한 인증 토큰이 필요합니다"
}
```

### 404 Not Found (일정 없음)
```json
{
  "error": "Schedule not found",
  "message": "요청한 일정이 존재하지 않거나 접근 권한이 없습니다"
}
```

### 400 Bad Request (유효성 오류)
```json
{
  "error": "Invalid date range",
  "message": "종료일이 시작일보다 이전일 수 없습니다"
}
```

---

## 주요 변경사항 요약 (v2)

### 새로 추가된 기능:
1. **PATCH /api/schedules/{id}**: 일정 부분 수정
2. **DELETE /api/schedules/{id}**: 일정 삭제

### 새로 추가된 필드:
- `Schedule` 엔티티:
  - `originalGoal`: 사용자가 입력한 원래 목표 (수정 불가)
  - `goalSummary`: 목표 요약/설명 (수정 가능)

- `AiScheduleRequest`:
  - `title`: 표시용 제목 (선택, 생략 시 goal을 제목으로 사용)

- `ScheduleCreateRequest`:
  - `originalGoal`: 원래 목표 (선택)
  - `goalSummary`: 목표 요약 (선택)

### UI 연동 시 활용:
- **목표 입력 화면**: `goal` (AI용) 또는 `title` (수동용)
- **목표 설명 표시**: `goalSummary` (AI가 자동 생성 또는 사용자 직접 입력)
- **프로젝트 이름**: `title`
- **원본 목표 보관**: `originalGoal` (수정 이력 추적용)

---

## Swagger UI 접속

서버 실행 후 브라우저에서:
```
http://localhost:8080/swagger-ui/index.html
```

모든 API를 웹에서 직접 테스트할 수 있습니다!
