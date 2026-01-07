# 🚀 Floorida Backend CI/CD 자동화 가이드

## 📋 개요

`main` 브랜치에 코드가 푸시/머지되면 자동으로:
1. **디스코드 알림** 발송
2. **클라우드타입 배포** 실행

---

## 🔄 자동화 흐름

```
feature 브랜치에서 작업
        ↓
   Pull Request 생성
        ↓
   코드 리뷰 & 승인
        ↓
   main 브랜치에 Merge
        ↓
  ┌─────────────────────────────┐
  │  GitHub Actions 자동 실행   │
  │  1. 디스코드 알림 발송 📢    │
  │  2. 클라우드타입 배포 🚀     │
  └─────────────────────────────┘
        ↓
   배포 완료! ✅
```

---

## 📢 디스코드 알림

- **트리거**: `main` 브랜치에 push/merge
- **알림 내용**: 커밋 정보, 변경 내용 요약
- **채널**: 프로젝트 디스코드 서버

---

## ☁️ 클라우드타입 자동 배포

### 배포 정보

| 항목 | 값 |
|------|-----|
| 플랫폼 | 클라우드타입 (Cloudtype) |
| 스페이스 | `hyeonhanjo1` |
| 프로젝트 | `floorida` |
| 서비스 | `back` |
| Java 버전 | 21 |
| 배포 URL | https://app.floorida.site |

### 배포 소요 시간

- GitHub Actions 실행: ~15초
- 클라우드타입 빌드 & 배포: ~3-5분
- **총 소요 시간: 약 5분**

---

## ⚙️ 환경변수 관리

### 중요! 환경변수는 클라우드타입 콘솔에서 관리합니다.

1. [클라우드타입 콘솔](https://app.cloudtype.io) 접속
2. `hyeonhanjo1/floorida` → `back` 서비스 선택
3. **설정** 탭 → **Environment variables**

### 등록된 환경변수 목록

- `DB_URL` - Supabase PostgreSQL 연결 URL
- `DB_USERNAME` - DB 사용자명
- `DB_PASSWORD` - DB 비밀번호
- `JWT_SECRET` - JWT 토큰 시크릿
- `OPENAI_API_KEY` - OpenAI API 키
- `AWS_S3_*` - S3 관련 설정
- `SPRING_MAIL_*` - 메일 발송 설정
- 그 외 앱 설정들

---

## 🛠️ 개발 워크플로우

### 1. 새 기능 개발 시

```bash
# 1. main에서 feature 브랜치 생성
git checkout main
git pull origin main
git checkout -b feature/새기능이름

# 2. 개발 작업...

# 3. 커밋 & 푸시
git add .
git commit -m "feat: 새 기능 설명"
git push origin feature/새기능이름

# 4. GitHub에서 Pull Request 생성
# 5. 리뷰 후 main에 Merge → 자동 배포!
```

### 2. 긴급 수정 시 (Hotfix)

```bash
# main에서 직접 작업 (주의!)
git checkout main
git pull origin main

# 수정 작업...

git add .
git commit -m "fix: 긴급 수정 내용"
git push origin main
# → 즉시 자동 배포!
```

---

## ⚠️ 주의사항

1. **main 브랜치는 항상 배포 가능한 상태**여야 합니다
2. **직접 main에 push하는 것은 피해주세요** (PR 사용 권장)
3. 환경변수 추가/변경 시 **클라우드타입 콘솔**에서 직접 수정
4. 배포 실패 시 GitHub Actions 로그 확인

---

## 🔍 문제 해결

### 배포 실패 시

1. **GitHub Actions 로그 확인**
   - 레포지토리 → Actions 탭 → 실패한 워크플로 클릭

2. **클라우드타입 빌드 로그 확인**
   - 클라우드타입 콘솔 → 서비스 → 배포 내역

### 앱 실행 오류 시

1. 클라우드타입 콘솔 → 서비스 → **로그** 탭 확인
2. 환경변수 누락 확인
3. DB 연결 확인

---

## 📁 관련 파일

- `.github/workflows/deploy.yml` - GitHub Actions 워크플로 설정
- `build.gradle` - Gradle 빌드 설정

---

## 👥 담당자

- CI/CD 설정 문의: (담당자 이름)
- 클라우드타입 계정: hyeonhanjo1

---

*마지막 업데이트: 2026-01-07*
