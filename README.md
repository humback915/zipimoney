# ZIPIMONEY

내 집 마련까지 걸리는 시간을 계산해주는 서비스.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.3, Java 21, JPA, Flyway |
| Frontend | Vite 6, React 19, TypeScript, Tailwind CSS 4 |
| Database | PostgreSQL 16 |
| Server | Undertow |
| Auth | JWT (httpOnly Cookie) + Kakao OAuth |
| Cache | Caffeine (5분 TTL) |
| Docs | Springdoc OpenAPI (Swagger UI) |

## 프로젝트 구조

```
homeclock-spring/
├── src/main/java/kr/zipimoney/
│   ├── domain/
│   │   ├── auth/          # Kakao OAuth, JWT 인증
│   │   ├── user/          # 사용자 엔티티
│   │   ├── profile/       # 프로필 (AES-256-GCM 암호화)
│   │   ├── consent/       # 동의 이력
│   │   ├── region/        # 지역 (법정동코드)
│   │   ├── realestate/    # 아파트 실거래가 (data.go.kr)
│   │   ├── calculate/     # 내 집 마련 계산기
│   │   ├── share/         # 공유 카드
│   │   └── stats/         # 소득 통계 스냅샷
│   └── global/
│       ├── config/        # Security, Web, Cache, Swagger, SPA
│       ├── security/      # JWT Provider, Filter, @CurrentUser
│       ├── crypto/        # AES-256-GCM 암복호화
│       ├── external/      # Kakao, data.go.kr RestClient
│       ├── entity/        # BaseEntity
│       ├── response/      # ApiResponse<T>
│       ├── exception/     # DomainException, GlobalExceptionHandler
│       └── scheduler/     # 실거래가 동기화 스케줄러
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/      # Flyway 마이그레이션
├── frontend/               # Vite + React SPA
│   ├── src/
│   │   ├── api/           # API 클라이언트
│   │   ├── hooks/         # useAuth, useGeolocation
│   │   ├── lib/           # calculator, meme, types
│   │   ├── pages/         # HomePage, KakaoCallback, SharePage
│   │   └── stores/        # Zustand 상태관리
│   └── vite.config.ts
├── docker-compose.yml      # PostgreSQL
└── Dockerfile              # 멀티스테이지 빌드
```

## 실행 방법

### 사전 요구사항

- Java 21+
- Node.js 18+
- Docker

### 1. PostgreSQL 실행

```bash
docker compose up -d
```

### 2. Backend 실행

```bash
./gradlew bootRun
```

서버가 http://localhost:8080 에서 시작됩니다.

### 3. Frontend 실행 (개발 모드)

```bash
cd frontend
npm install
npm run dev
```

http://localhost:3000 에서 시작되며, `/api` 요청은 백엔드로 프록시됩니다.

### 4. 통합 빌드 (단일 JAR)

```bash
./gradlew build -PwithFrontend
```

프론트엔드 빌드 결과가 `resources/static/`에 포함된 단일 JAR이 생성됩니다.

### 5. Docker 빌드

```bash
docker build -t zipimoney .
docker run -p 8080:8080 zipimoney
```

## API 문서

서버 실행 후 Swagger UI에서 확인: http://localhost:8080/swagger-ui.html

## 주요 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/kakao` | 카카오 로그인 | - |
| POST | `/api/auth/refresh` | 토큰 갱신 | - |
| POST | `/api/auth/logout` | 로그아웃 | - |
| GET | `/api/auth/me` | 내 정보 | O |
| POST | `/api/auth/withdraw` | 회원 탈퇴 | O |
| GET | `/api/region` | 지역 목록 | - |
| GET | `/api/geocode` | 좌표→지역 변환 | - |
| GET | `/api/search` | 장소 검색 | - |
| GET | `/api/deals` | 실거래가 조회 | - |
| GET | `/api/complex` | 단지별 거래 | - |
| GET/PUT | `/api/profile` | 프로필 조회/저장 | O |
| GET/POST | `/api/consent` | 동의 조회/기록 | O |
| POST | `/api/calculate` | 계산 실행 | 선택 |
| GET | `/api/history` | 계산 이력 | O |
| POST | `/api/share` | 공유 카드 생성 | 선택 |
| GET | `/api/share/{key}` | 공유 카드 조회 | - |

## 환경 변수

`application.yml`에서 설정하거나 환경변수로 오버라이드:

| 변수 | 설명 |
|------|------|
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret |
| `KAKAO_REDIRECT_URI` | 카카오 OAuth 리다이렉트 URI |
| `JWT_SECRET` | JWT 서명 키 (Base64, 256bit+) |
| `PROFILE_ENCRYPTION_KEY` | 프로필 암호화 키 (Base64, 256bit) |
| `DATA_GO_KR_API_KEY` | 공공데이터포털 API 키 |
| `CRON_SECRET` | 수동 동기화 시크릿 |

## 테스트

```bash
./gradlew test
```
