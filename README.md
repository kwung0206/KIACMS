# KIACMS

KIACMS(Korea IT Academy Class Management System)는 교육 운영 현장에서 바로 사용할 수 있도록 설계한 AI 기반 수업 관리 플랫폼입니다.  
단순 LMS가 아니라 학생, 강사, 멘토, Root 관리자를 하나의 서비스로 연결해 수업 일정, 정리글, 프로젝트 모집, 알림, AI 도우미까지 함께 제공합니다.

## 프로젝트 구성

- `backend/` : Spring Boot 백엔드 API
- `frontend/` : React + Vite 프론트엔드
- `infra/` : 로컬 개발용 PostgreSQL 등 인프라 설정
- `docs/` : 아키텍처/설계 문서
- `scripts/` : 샘플 데이터 및 보조 스크립트

## 현재 구현 요약

### 공통

- 로그인 / 회원가입 / 승인 대기 흐름
- 역할 기반 라우팅 및 접근 제어
- 알림 조회 / 읽음 / 삭제
- 상단 AI 도우미 챗봇
- 마이페이지

### 학생

- 수업 캘린더 조회
- 내 노트 목록 / 작성 / 수정 / 상세
- 노트 캘린더에서 수업 일정 함께 표시
- 날짜 클릭 시 노트 작성으로 이동
  - 수업이 있으면 수업 선택 모달 표시
  - 수업이 없으면 바로 작성 화면 이동
  - 선택한 수업의 `courseId`, `courseSessionId` 자동 입력
- 프로젝트 목록 / 상세 / 모집글 작성 / 내 지원 현황

### 강사

- 담당 회차 조회
- 태그된 노트 목록 / 상세
- 노트 코멘트 작성

### 멘토

- 담당 학생 관리
- 학생 선택 모달
- 학생-수업 매핑

### Root

- 승인 관리
- 수업 일정 관리
- 프로젝트 관리

## 기술 스택

- Backend: Spring Boot, Spring Security, Spring Data JPA
- Frontend: React, Vite, React Router
- Database: PostgreSQL
- AI: OpenAI API

## 빠른 실행 방법

### 1. DB 실행

`.env.example`을 `.env`로 복사한 뒤 실행합니다.

```powershell
docker compose -f infra/docker-compose.yml --env-file .env up -d
```

중지:

```powershell
docker compose -f infra/docker-compose.yml --env-file .env down
```

### 2. 백엔드 실행

```powershell
cd backend
.\gradlew.bat bootRun
```

기본 포트:

- `http://localhost:8085`

### 3. 프론트엔드 실행

```powershell
cd frontend
npm install
npm run dev
```

기본 포트:

- `http://localhost:5173`

## 프론트 환경변수

`frontend/.env` 파일 예시:

```env
VITE_API_BASE_URL=http://localhost:8085
```

## 백엔드 주요 환경변수

예시:

```env
KIACMS_SERVER_PORT=8085
KIACMS_DB_HOST=localhost
KIACMS_DB_PORT=5432
KIACMS_DB_NAME=kiacmsdb
KIACMS_DB_USER=postgres
KIACMS_DB_PASSWORD=your-password
OPENAI_API_KEY=your-openai-key
```

주의:

- `OPENAI_API_KEY`는 절대 프론트엔드에 넣지 않습니다.
- 실제 운영용 비밀번호나 키는 저장소에 커밋하지 않습니다.

## 테스트용 샘플 계정

아래 계정은 빠른 기능 테스트용입니다.

### Root

- 아이디: `root@kiacms.local`
- 비밀번호: `Test1234!`

### 학생

- 아이디: `student@kiacms.local`
- 비밀번호: `Test1234!`

### 멘토

- 아이디: `mentor@kiacms.local`
- 비밀번호: `Test1234!`

### 강사

- 아이디: `teacher@kiacms.local`
- 비밀번호: `Test1234!`

추가 샘플 학생 계정도 존재합니다.

- 예시: `seed.student01@kiacms.local`
- 비밀번호: `Test1234!`

## 샘플 데이터

현재 샘플 데이터에는 다음이 포함됩니다.

- 학생 30명
- 강사 5명
- 프로젝트 3개
- 과정 및 회차 데이터
- 프로젝트 지원서 / 멘토 매핑 / 알림 일부 샘플

샘플 데이터 정리 스크립트:

- [cleanup_malformed_demo_data.py](C:/Users/kgj01/Documents/KIACMS/scripts/cleanup_malformed_demo_data.py)

## 인증 동작 방식

- 로그인 성공 시 access token을 `localStorage`에 저장
- API 호출 시 `Authorization: Bearer <token>` 자동 주입
- `401` 발생 시 저장된 인증 정보 제거 후 로그인 흐름으로 복귀

## 현재 확인된 주요 기능

- 학생 노트 캘린더와 수업 일정 통합 표시
- 멘토 학생 선택 모달
- 프로젝트 게시판 / 상세 / 지원 흐름
- 알림 삭제
- AI 도우미 기본 동작

## 남은 개선 포인트

- 프로젝트/운영 화면의 추가 한글화 정리
- Root/멘토 운영 대시보드 고도화
- 노트 작성 시 강사 검색형 태그 UX 추가
- 프로젝트 수정/삭제/마감 제어 고도화
- 사용자 정보 수정 API 및 화면 확장

## 문서

- [1단계 아키텍처 문서](docs/architecture/step-01-system-architecture.md)
- [2단계 ERD 및 엔티티 문서](docs/architecture/step-02-erd-and-entities.md)
- [3단계 백엔드 초기 구조 문서](docs/architecture/step-03-backend-bootstrap.md)
