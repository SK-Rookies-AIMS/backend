# backend-main

`backend-main`은 AIMS 알림/이벤트 처리 API를 제공하는 Spring Boot 백엔드 서비스입니다. 알림 조회, 상세 확인, 조치 처리, AI 대응 추천, 인증, Gateway 연동을 담당합니다.

## 실행 프로파일

`backend-main`은 `local`, `dev`, `prod` 프로파일을 사용합니다. 기본 프로파일은 `local`이며, `SPRING_PROFILES_ACTIVE` 환경 변수로 변경합니다.

| 프로파일 | 용도 | 주요 설정 |
| --- | --- | --- |
| `local` | 로컬 개발 환경 | `.env` 기반 로컬 DB/Redis/Kafka/OpenSearch 접속, 상세 SQL 로그 활성화 |
| `dev` | 개발 서버 환경 | 개발 서버용 외부 의존성 접속, Kafka group 기본값 `backend-dev`, 개발 로그 레벨 |
| `prod` | 운영 환경 | 운영 서버용 외부 의존성 접속, Kafka group 기본값 `backend-prod`, 운영 로그 레벨 |

프로파일 변경 예시:

```properties
SPRING_PROFILES_ACTIVE=local
```

Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
.\gradlew.bat bootRun
```

프로파일별 설정 파일:

- 공통 설정: `src/main/resources/application.yaml`
- 로컬 설정: `src/main/resources/application-local.yaml`
- 개발 설정: `src/main/resources/application-dev.yaml`
- 운영 설정: `src/main/resources/application-prod.yaml`

## 주요 기능

### event

알림과 이벤트 처리 흐름을 담당합니다.

| 기능 | 설명 | 권장 API |
| --- | --- | --- |
| 알림 목록 조회 | 조건, 상태, 기간, 키워드 기반 필터 검색 | `GET /api/events/alerts` |
| 알림 상세 조회 | 단일 알림의 상세 정보 조회 | `GET /api/events/alerts/{alertId}` |
| 조치 이력 조회 | 알림별 조치 이력 조회 | `GET /api/events/alerts/{alertId}/actions` |
| AI 대응 추천 조회 | 알림 원인과 상황 기반 AI 대응 추천 조회 | `GET /api/events/alerts/{alertId}/ai/recommendation` |
| AI 매뉴얼 | 알림 유형별 AI 매뉴얼 조회 | `GET /api/events/alerts/{alertId}/ai/manual` |
| AI 신뢰성 평가 | AI 추천 결과에 대한 신뢰도/근거 평가 조회 | `GET /api/events/alerts/{alertId}/ai/reliability` |
| 알림 요약 조회 | 알림 상태, 유형, 심각도 기준 요약 조회 | `GET /api/events/alerts/summary` |
| 오늘 이벤트 현황 | 당일 발생 이벤트 통계 조회 | `GET /api/events/today` |
| 알림 조치 처리 | 담당자 조치 내용 등록 및 상태 변경 | `POST /api/events/alerts/{alertId}/actions` |
| 조치 불필요 처리 | 알림을 조치 불필요 상태로 변경 | `PATCH /api/events/alerts/{alertId}/dismiss` |

## 인증

인증은 JWT Bearer Token 기반입니다.

- 인증 헤더: `Authorization: Bearer {accessToken}`
- Access Token 만료 시간: `JWT_ACCESS_EXPIRATION`
- Refresh Token 만료 시간: `JWT_REFRESH_EXPIRATION`
- JWT secret: `JWT_SECRET_KEY`

현재 Security 설정:

- Stateless session
- CSRF, form login, HTTP basic 비활성화
- JWT 필터: `JwtAuthenticationFilter`
- 토큰 생성/검증: `TokenProvider`
- 공개 경로:
  - `/`
  - `/api/test`
  - `/actuator/health`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`
  - `/swagger-resources/**`
  - `/webjars/**`
- 그 외 API는 인증 필요

## Gateway 설정

Gateway는 외부 요청을 `backend-main`으로 라우팅하고 JWT 인증 헤더를 전달합니다.

권장 라우팅:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: backend-main
          uri: http://backend-main:8080
          predicates:
            - Path=/api/events/**,/api/test/**,/v3/api-docs/backend/**
          filters:
            - StripPrefix=0
```

Gateway 연동 시 유지해야 하는 헤더:

- `Authorization`
- `Content-Type`
- `X-Request-Id`

CORS는 `WebConfig`에서 처리하며, 허용 origin은 `cors.allowed-origins` 설정으로 관리합니다.

## 기술 스택

- Java 17
- Spring Boot 4.0.6
- Spring WebMVC
- Spring Security
- Spring Data JPA
- QueryDSL
- MySQL, H2
- Redis Cache
- Kafka
- OpenSearch Java Client
- Spring Cloud OpenFeign
- Springdoc OpenAPI
- JWT
- Spring Boot Actuator
- Spring Boot Admin Client

## 패키지 구조

현재 기본 패키지는 `com.aims.backend`입니다.

```text
src/main/java/com/aims/backend
+-- BackendApplication.java
+-- common
|   +-- code
|   +-- response
|   +-- status
+-- config
|   +-- jwt
|   +-- security
+-- controller
+-- domain
|   +-- commons
+-- dto
|   +-- auth
|   +-- test
+-- exception
|   +-- handler
+-- mapper
+-- properties
+-- repository
+-- service
+-- utils
```

권장 event 패키지 확장:

```text
com.aims.backend
+-- controller/event
+-- dto/event
+-- domain/event
+-- repository/event
+-- service/event
+-- mapper/event
```

## 설정

프로젝트 루트의 `.env`를 자동으로 읽습니다.

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

주요 환경 변수:

| 변수 | 설명 |
| --- | --- |
| `APP_NAME` | Spring application name |
| `SPRING_PROFILES_ACTIVE` | 실행 profile |
| `MAIN_DB_JDBC_URL` | Main DB JDBC URL |
| `MAIN_DB_USERNAME` | Main DB 계정 |
| `MAIN_DB_PASSWORD` | Main DB 비밀번호 |
| `SAMPLE_DB_JDBC_URL` | Sample DB JDBC URL |
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers |
| `KAFKA_GROUP_ID` | Kafka consumer group id |
| `OPENSEARCH_HOST` | OpenSearch host |
| `OPENSEARCH_PORT` | OpenSearch port |
| `JWT_SECRET_KEY` | JWT signing key |

## 실행

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

## 테스트

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```

테스트 profile은 H2 in-memory DB를 사용합니다.

## API 문서

로컬 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```
