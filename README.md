# 콘서트 예약 시스템

## 📋 프로젝트 개요

### 요구사항
* 유저 토큰 발급 API
* 예약 가능 날짜 / 좌석 API
* 좌석 예약 요청 API
* 잔액 충전 / 조회 API
* 결제 API
* 각 기능 및 제약사항에 대해 단위 테스트를 반드시 하나 이상 작성
* 다수의 인스턴스로 어플리케이션이 동작하더라도 기능에 문제가 없도록 작성
* 동시성 이슈를 고려하여 구현
* 대기열 개념을 고려해 구현

### 마일스톤
* 1주차: [콘서트 티켓팅 예약 시스템 설계](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218480) (API 명세, 시퀀스 다이어그램, ERD, 마일스톤 작성, Mock API 작성)
* 2주차: [포인트 충전 / 조회 기능 구현](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218504), [대기열 시스템 구현](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218492)
* 3주차: [콘서트 좌석 예약](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218503), [결제 구현](https://github.com/users/ByeonJuHwan/projects/7/views/4?pane=issue&itemId=69218505)

### 설계 문서
* [API 명세서](https://github.com/ByeonJuHwan/Concert-Ticketing/wiki/%EC%BD%98%EC%84%9C%ED%8A%B8-%EC%98%88%EC%95%BD-%EC%84%9C%EB%B9%84%EC%8A%A4-API-%EB%AA%85%EC%84%B8%EC%84%9C)
* [시퀀스 다이어그램](https://github.com/ByeonJuHwan/Concert-Ticketing/wiki/%EC%BD%98%EC%84%9C%ED%8A%B8-%EC%A2%8C%EC%84%9D-%EC%98%88%EC%95%BD-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%8B%9C%ED%80%80%EC%8A%A4-%EB%8B%A4%EC%9D%B4%EC%96%B4%EA%B7%B8%EB%9E%A8)
* [ERD](https://github.com/ByeonJuHwan/Concert-Ticketing/wiki/%EC%BD%98%EC%84%9C%ED%8A%B8-%EC%A2%8C%EC%84%9D-%EC%98%88%EC%95%BD-ERD)

---

## 🏗️ 아키텍처 진화 과정

### 1단계: Monolithic 기반 핵심 기능 구현
모놀리식 아키텍처로 시작하여 예약 시스템의 핵심 기능을 구현하고 다양한 기술적 문제를 해결했습니다.

#### 동시성 제어
- **[좌석 예약 동시성 이슈 해결](docs/synchronicity.md)**
  - 동일한 좌석에 대한 동시 예약 요청 문제 해결
  - 락을 사용할때의 트랜잭션 범위 설정

#### 성능 최적화
- **[인덱스 설계 및 쿼리 최적화](docs/index.md)**
  - 데이터베이스 조회 최적화 위한 인덱스 설계
  - 조회 API 성능 개선 사례 및 측정 결과

- **[캐싱 전략 수립](docs/caching.md)**
  - 로컬캐싱, Redis 를 사용한 캐싱 작성
  - 캐싱 적용 전후 성능 비교

#### 이벤트 기반 아키텍처 도입
- **[이벤트 드리븐 방식 적용](docs/event.md)**
  - `ApplicationEventPublisher` 에서 `Kafka`로의 전환 과정
  - 이벤트 기반 서비스 간 통신 구현

- **[아웃박스 패턴 구현](docs/outbox.md)**
  - 이벤트 발행 보장을 위한 아웃박스 패턴
  - 배치/스케줄러를 통한 미발행 이벤트 처리

---

### 2단계: MSA 전환 및 분산 시스템 구축
대용량 트래픽 처리를 위해 마이크로서비스 아키텍처로 전환하고, 분산 시스템에서 발생하는 문제들을 해결했습니다.

#### 아키텍처 전환
- **[모놀리식 → MSA 전환 과정](docs/msa/msa-convert.md)**
  - MSA 전환 전략 및 서비스 분리 기준
  - 전환 과정에서 발생한 기술적 문제 및 해결 방안
  - 성능 개선 결과 (200 TPS → 2000+ TPS)

- **[API Gateway 도입](docs/msa/gateway.md)**
  - Interceptor에서 API Gateway로의 전환
  - 토큰 검증 로직 중앙화
  - 서비스별 중복 코드 제거 및 유지보수성 향상

#### 서비스 간 통신 최적화
- **[gRPC 도입](docs/grpc/grpc.md)**
  - REST API에서 gRPC로 전환한 이유
  - gRPC 기반 서비스 간 통신 구현
  - 성능 개선 결과 및 장단점 분석

#### 분산 시스템 안정성 확보
- **[Circuit Breaker 적용](docs/msa/circuit-breaker.md)**
  - Resilience4j를 활용한 장애 전파 방지
  - 서비스 안정성 향상 전략

- **[SAGA 패턴 구현](docs/msa/saga-pattern.md)**
  - 분산 트랜잭션 관리 및 최종적 일관성 보장
  - 자동 재시도 전략 구현

- **[최종적 일관성 구현](docs/msa/eventual-consistency.md)**
  - `Kafka` 기반 이벤트 발행을 통한 데이터 복제
  - 서비스 간 결합도 감소 및 데이터 동기화 전략

#### 검색 시스템 개선
- **[Elasticsearch 도입](docs/elasticsearch.md)**
  - MySQL LIKE 쿼리에서 `Elasticsearch`로 전환
  - 자동완성 및 퍼지 검색 구현
  - 콘서트 검색 성능 향상

---

## 📊 성능 테스트 및 운영

### JPA READ ONLY 최적화
- **[JPA READ ONLY 쿼리 최적화](docs/transactional-read-only.md)**
  - 조회 전용 트랜잭션 설정을 통한 성능 향상

### 부하 테스트
- **[부하테스트 결과](docs/performance.md)**
  - 특정 시간 트래픽 급증 시나리오 테스트
  - 각 API별 동시 사용자 처리 성능 측정
  - 대기열 시스템 효과 검증

### 장애 대응
- **[장애 대응 문서](docs/response.md)**
  - 실제 운영 환경에서의 장애 사례 및 해결 과정
  - 장애 예방을 위한 모니터링 및 알람 체계

---

## 🛠 기술 스택

### Backend
- Kotlin, Spring Boot
- Spring Data JPA, MyBatis
- Kafka (이벤트 스트리밍)
- gRPC, Protocol Buffers

### Database
- MySQL (주 데이터베이스)
- Redis (캐싱, 분산락)
- Elasticsearch (검색)

### Infrastructure
- Docker, Docker Compose
- Resilience4j (Circuit Breaker)

### Testing
- TestContainers (통합 테스트)
- JUnit 5

---

## 📂 프로젝트 구조
```
Concert-Ticketing/
├── concert-service/      # 콘서트 관리 서비스
├── payment-service/      # 결제 서비스
├── token-service/        # 대기열 토큰 서비스
├── user-service/         # 사용자 관리 서비스
└── gateway-service/      # API Gateway
```