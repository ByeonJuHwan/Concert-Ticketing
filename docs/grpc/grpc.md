# GRPC 도입

현재 콘서트 예약 시스템은 MSA 아키텍처로 구성되어 있으며, Payment, Concert, User 등 여러 서비스 간의 통신이 빈번하게 발생합니다.

특히 결제 프로세스에서는 서비스 간 통신이 연쇄적으로 발생합니다.

현재 이러한 모든 통신은 HTTP/1.1 기반의 RESTful API로 구현되어 있습니다.

RESTful API는 널리 사용되는 API 이지만 장점만 있는것은 아니었습니다

- 연결 오버헤드 : 매 요청마다 TCP `handshake` HTTP 헤더 파싱이 반복됩니다.
- Head-of-Line Blocking : HTTP/1.1은 요청을 순차적으로 처리하기 때문에, 
  하나의 느린 요청이 뒤따르는 요청들을 지연시킵니다.
- 비효율적인 직렬화: JSON 기반 데이터 전송으로 인한 파싱 오버헤드와 네트워크 대역폭 낭비가 발생합니다.

이로 인해서 네트워크 리소스 낭비, 높은 지연 시간 문제등이 발생하고 있었습니다.

위 문제를 해결하기 위해 gRPC를 도입하기로 결정했습니다.

## 왜 gGPC 인가?

다른 대안들도 검토했습니다. HTTP/2 기반 REST API나 GraphQL도 고려 대상이었지만, 
**서비스 간 내부 통신**이라는 우리의 사용 사례에서 gRPC가 가장 적합하다고 판단했습니다.

- HTTP/2 REST: 여전히 JSON 직렬화 오버헤드 존재
- GraphQL: 클라이언트 주도 쿼리에는 유용하지만, 서버 간 통신에는 과도한 복잡성
- **gRPC**: HTTP/2 + Protocol Buffers로 성능과 타입 안정성 모두 확보

우선 HTTP/1.1의 문제점을 gRPC에서 어떻게 해결했는지 알아보겠습니다.

### 멀티플랙싱 

- **단일 TCP 연결에서 멀티플랙싱 사용** : 하나의 연결로 여러 요청을 동시에 처리합니다
- 연결 재사용으로 `handshake` 오버헤드를 제거합니다
- 헤더의 경우 매 요청마다 전송하지만 이미 전송된 헤더의 경우 인덱스를 바라보도록해 헤더 파싱 오버헤드를 줄입니다.

[멀티플랙싱 동작 확인 문서](multiplexing.md)

큰 장점이지만 직접 테스트 결과 순차적으로 동작하는 경우 멀티플랙싱 장점이 제대로 드러나지 않았습니다.

---

### Protocol Buffers를 통한 효율적인 데이터 직렬화

**JSON**
- 텍스트 기반으로 데이터 크기가 큼
- 필드명이 매번 전송됨
- 파싱 오버헤드 큼

**gRPC 개선**
- **바이너리 직렬화**로 데이터 크기 약 60~80% 감소
- 필드명 대신 필드 변호 사용
- 빠른 인코딩/디코딩 속도

실제로 `Protocol Buffers` 를 사용해서 데이터 직렬화를 하면 얼마나 데이터가 감소하는지 테스트 및 확인해본 결과 약 84% 이상의 크기 절감 효과가 있었습니다.

[직렬과 크기 비교 문서](binary-serialization.md)

---

## 도입 과정

#### 새로운 인터페이스, 구현체

프로젝트의 각 레이어는 전부 인터페이스화되어 있어서 구현체만 변경하면 되겠지 라고 생각했습니다.

하지만 실제로 코틀린의 gRPC의 경우 코루틴을 사용하므로 `suspend` 키워드가 필수였기 때문에, 
새로운 인터페이스 및 구현체를 작성하는 것부터 시작했습니다.

gRPC의 기본 구조 자체가 코루틴이다 보니, 이번 기회를 통해서 코루틴이 무엇이고 
어떤 점이 기존 자바의 멀티스레드 방식보다 더 좋은지 확인해 보았습니다.

[코루틴 문서](coroutine.md)
```kotlin
interface PaymentGrpcUseCase {
    suspend fun pay(command: PaymentCommand): PaymentResponse
}
```

#### proto 파일 생성

gRPC의 경우 proto 파일을 통해서 통신하기 때문에, HTTP 통신과 비슷하게 request, response에 대한 응답값을 만들어줘야 합니다.
```protobuf
service ConcertService {
  // 결제
  rpc GetReservation(GrpcGetReservationRequest) returns (GrpcConcertReservationResponse);
  rpc ReservationExpiredAndSeatAvailable (GrpcReservationExpiredAndSeatAvailableRequest) returns (GrpcReservationExpiredAndSeatAvailableResponse);
  rpc ChangeReservationPaid (GrpcChangeReservationPaidRequest) returns (GrpcChangeReservationPaidResponse);
  rpc ChangeSeatReserved (GrpcChangeSeatReservedRequest) returns (GrpcChangeSeatReservedResponse);

  // 결제 취소
  rpc ChangeReservationPending(GrpcChangeReservationPendingRequest) returns (GrpcChangeReservationPendingResponse);
  rpc ChangeSeatTemporarilyAssigned(GrpcChangeSeatTemporarilyAssignedRequest) returns (GrpcChangeSeatTemporarilyAssignedResponse);
}
```

proto 파일 작성 시 주의했던 점은 명확한 네이밍과 각 서비스별 책임 분리였습니다. 
결제와 결제 취소를 명확히 구분하고, 각 RPC 메서드가 단일 책임만 갖도록 설계했습니다.

#### SAGA 패턴 적용

기존 HTTP 통신에 사용하던 SAGA 패턴을 인터페이스만 변경하여 gRPC로 변경했습니다

```kotlin
// 포인트 차감
val pointResponse = sagaGrpcExecution.executeStep(
    sagaId = sagaId,
    stepName = POINT_USE,
) {
    pointGrpcClient.use(
        userId = userId,
        amount = reservation.price,
    )
}
```

#### API 분리

현재 API 스펙상 버저닝을 진행하고 있습니다 → 예시: /api/v1/concert/reserve

운영 환경에서 HTTP 통신만을 통해서 정상적으로 운영되고 있는 상황에서, 
모든 트래픽을 내부 테스트만 진행한 gRPC로 변경한다는 건 리스크가 있다고 판단했습니다.

해서 gRPC를 사용한 결제의 경우 v2로 진행하여, 요청의 10%만 통과시켜 
실제 운영 환경에서 모니터링 후 점진적으로 변경하는 방법을 채택할 것 같습니다.

---

## 성능 비교

이제 실제 k6를 통해서 gRPC 와 HTTP1.1 의 성능을 비교해보았습니다

동시요청 상황에서 gRPC의 멀티플랙싱 이점이 생기다보니 임의로 동시요청 API 작성 후 테스트를 진행했습니다.

```kotlin
// gRPC
val (payments, reservations) = coroutineScope {
    val paymentsDeferred = async { userPaymentGrpcClient.searchUserPayments(userId) }
    val reservationsDeferred = async { userPaymentGrpcClient.searchUserReservations(userId) }

    paymentsDeferred.await() to reservationsDeferred.await()
}

// HTTP/1.1
val paymentsFuture = CompletableFuture.supplyAsync {
    userPaymentHttpClient.searchUserPayments(userId)
}

val reservationsFuture = CompletableFuture.supplyAsync {
    userPaymentHttpClient.searchUserReservations(userId)
}

val payments = paymentsFuture.get()
val reservations = reservationsFuture.get()
```

### 테스트 환경
- 도구: K6 (부하 테스트)
- 부하 시나리오:
  - 워밍업: 10 VUs (30초)
  - 램프업: 50 VUs (1분)
  - 유지: 50 VUs (2분)
  - 스파이크: 100 VUs (30초)
  - 유지: 100 VUs (1분)
  - 램프다운: 0 VUs (30초)
- 총 테스트 시간: 5분 30초
- 각 VU는 1초 간격으로 요청 반복

### 측정 지표
- TPS (Transactions Per Second): 처리량
- 응답 시간: 평균, P50, P90, P95, P99, 최대값
- 에러율
- 네트워크 전송량


## 성능 테스트 결과

### 전체 비교표

| 메트릭 | REST API | gRPC | 개선율 |
|--------|----------|------|--------|
| **TPS** | 53.12 req/s | 53.10 req/s | ~동일 |
| **평균 응답시간** | 7.69ms | 7.33ms | **4.7% ↓** |
| **중앙값 (P50)** | 5.31ms | 4.91ms | **7.5% ↓** |
| **P90** | 16.39ms | 16.14ms | **1.5% ↓** |
| **P95** | 21.49ms | 19.37ms | **9.9% ↓** |
| **P99** | 30.8ms | 27.17ms | **11.8% ↓** |
| **최대 응답시간** | 234.73ms | 194.97ms | **16.9% ↓** |
| **총 요청 수** | 17,564개 | 17,571개 | ~동일 |
| **에러율** | 0% | 0% | ~동일 |

## 결과 분석

- **평균 응답시간**: 5% 개선
- **P95**: 10% 개선 (21.49ms → 19.37ms)
- **P99**: 12% 개선 (30.8ms → 27.17ms)
- **최악의 경우**: 17% 개선 (234.73ms → 194.97ms)

**의미**: 
- 대부분의 사용자(95~99%)가 더 빠른 응답을 경험
- 특히 **꼬리 지연(Tail Latency) 감소**가 두드러짐
- 네트워크 지연이 긴 상황에서 gRPC가 더 안정적

#### TPS는 동일한 이유
- 현재 테스트는 K6(HTTP) → API (REST) → Backend(gRPC) 구조
- 클라이언트와 서버 사이는 여전히 HTTP/REST 사용
- **내부 서비스 간 통신**에서만 gRPC의 이점 발휘
- VUs가 1초마다 요청하므로 처리량보다는 응답 속도에서 차이 발생

요약해보면 모든 응답 시간이 5~17% 개선되었으며, 특히 네트워크 상황이 좋지 않은 사용자 혹은 서버 부하가 높은 상황(P95~P99)에서 10~12%의 더 큰 개선 효과가 나타났습니다.

## 마무리

gRPC 의 장점도 많지만 도입하면서 장점만 있지는 않았습니다

코루틴기반이다보니 실제 코루틴이 무었인지, HTTP 1.1 과 HTTP 2 의 차이점은 무었인지 등등 실제 러닝커브가 있었습니다.

테스트및 실제 api 속도 테스트를 진행해보니 유의미한 차이점이 있었나? 제 기준에서는 그렇게 큰 차이점은 못느꼈습니다.

하지만 현재 제 프로젝트보다 더 많은 서비스(서버)와 통신하고,  동시요청이 많은 서비스라면 충분히 도입할만하다고 생각합니다.

이번 gRPC 도입 경험을 통해 **기술 선택은 단순히 '좋다/나쁘다'가 아니라'상황에 맞는가'가 중요**하다는 것을 다시 한번 깨달았습니다. 