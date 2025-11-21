# 로그인 & 세션 관리 API

## [개발 환경]
- Java: 17
- Spring Boot: 3.5.8
- DB: Mysql
- 빌드 툴: Gradle 
- 테스트: JUnit 5, Mockito(BDDMockito), AssertJ


## [실행방법]
1. docker-compose up -d
2. application main 실행


## [API 명세]
http://localhost:8080/swagger-ui/index.html


## [설계 의도 및 기술 선택 이유]
### 1. Spring Security
- 단순히 로그인 체크를 넘어 애플리케이션 보안 전반을 체계적으로 관리
- 인증/인가 로직을 안전하게 처리
- 반복되는 보안 코드를 줄이고, 실수로 인한 취약점 예방
- JWT, OAuth, Redis 등 다양한 전략과 쉽게 연계 가능
- 확장성과 유지보수성이 뛰어난 구조 제공

### 2. Access Token(헤더) + Refresh Token(HttpOnly Cookie) + Refresh Token 서버(Redis) 검증
- Access Token이 JS 메모리에만 있으므로 CSRF 공격 방어
- Refresh Token이 HttpOnly Cookie → XSS 방어
- Refresh Token 서버 저장(RTR) → 탈취 후 즉시 무효화 가능
- Access Token 갱신 시 요청 크기 최소화 → 성능 최고
- 완벽한 stateless 는 아니지만, 필요한 부분만 stateful 해서 보안 강화
- Access Token 만료 전 강제 로그아웃, 탈취 방지 등을 위해 블랙리스트 관리

### 3. Redis
- 세션과 토큰 관리를 분산 환경에서도 안전하고 효율적으로 처리
- 클러스터 환경에서 세션과 인증 상태 공유
- JWT Refresh Token 관리 및 보안 강화
- Access Token 블랙리스트 및 강제 로그아웃 처리
- 높은 성능과 TTL 기반 자동 만료 기능 활용

### 4. JPA + QueryDSL
- JPA는 객체 중심 개발을 가능하게 하고, 변경 감지·1차 캐시·지연 로딩 같은 성능 최적화 기능을 기본 제공하여 SQL 작성량을 줄임
- QueryDSL 활용 → 타입 안전한 쿼리 작성 가능
- 복잡한 조회는 Querydsl로 해결하며, 전반적으로 비즈니스 로직에 집중할 수 있는 구조

### 5. 책임 분리
- 단일 책임 원칙(SRP): 한 클래스, 한 컴포넌트가 하나의 책임만 담당
  - RedisService: Refresh 토큰 상태 저장/조회/삭제
  - AuthService: 로그인 비즈니스 로직
  - JwtProvider: 토큰 생성/검증
  - JwtFilter: JWT 검증 필터
  - UserDetailsServiceImpl: DB에서 사용자 정보 조회 및 권한 부여
- 의존성 주입(DI): 클래스 내부에서 직접 의존성을 만들지 않고 외부에서 주입 → 테스트 용이
- 인터페이스 기반 설계: 구현과 사용을 분리 → 교체 용이, Mocking 가능


## [TO-DO]
### 1. Redis 기반 Refresh Token 구조 고도화
- 기존 저장 구조 개선 (Hash 구조로 고도화)
- 다중 로그인 지원 (디바이스별 토큰 관리)
- 의심 로그인 탐지(IP, UA 기반)
- 세션 만료 시간 함께 저장

### 2. Refresh Token Rotation 보안 강화
- Replay 공격 방지 강화
- Rotation 진행 중 Race Condition 방지

### 3. Refresh Token 재사용 시 계정 잠금 옵션
- 이전 토큰 재사용 발견 → 강제 로그아웃 + 사용자 알림

### 4. Redis + Security 성능 최적화
- Redis Pool(TTL, TCP Keep-Alive) 튜닝
  - 기본값은 대규모 트래픽에 비효율적
- Token 검증 Redis 요청 최소화
  - 현재 방식은 매 요청 Redis 조회
  - Access Token은 Redis 조회 X (JWT 자체만 검증)

    


     