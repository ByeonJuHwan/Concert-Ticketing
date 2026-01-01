# 분산 트랜잭션: SAGA 패턴

## 문제 상황

`Monolithic` 환경에서는 하나의 데이터베이스에서 트랜잭션을 관리했기 때문에, 
`@Transactional` 어노테이션만으로 여러 도메인의 데이터 일관성을 보장할 수 있었습니다.

하지만 MSA 환경에서는 각 서비스가 독립적인 데이터베이스를 가지므로, 
하나의 비즈니스 로직이 여러 서비스에 걸쳐 실행될 때 트랜잭션 관리가 어려워집니다.

**예시: 콘서트 결제 프로세스**

1. `Concert-Service`: 예약 정보 조회 및 유효성 검증
2. `User-Service`: 포인트 차감, 포인트 사용 히스토리 저장
3. `Concert-Service`: 예약 상태 확정, 좌석 상태 확정, 예약 정보 저장

만약 2단계에서 포인트 차감이 실패하면 이미 점유된 좌석을 롤백해야 하지만, 
각 서비스의 트랜잭션이 독립적이기 때문에 자동 롤백이 되지 않습니다.

## 해결 방안: SAGA 패턴

이 문제를 해결하기 위해 **SAGA 패턴**을 적용했습니다.

SAGA 패턴은 각 서비스의 로컬 트랜잭션을 순차적으로 실행하고, 실패 시 보상 트랜잭션(Compensating Transaction)을 통해 이전 단계를 롤백합니다.

**SAGA 패턴의 동작 방식**
- 각 단계가 성공하면 다음 단계로 진행
- 특정 단계가 실패하면 이전에 실행된 모든 단계에 대해 보상 트랜잭션 실행
- 최종적 일관성(Eventual Consistency)을 보장

### SAGA 패턴 구현 방식

SAGA 패턴은 크게 두 가지 방식으로 구현할 수 있습니다.

1. **Choreography 방식**: 각 서비스가 이벤트를 발행하고 구독하여 자율적으로 동작
2. **Orchestration 방식**: 중앙 Orchestrator가 전체 흐름을 제어

이 프로젝트에서는 **Orchestration 방식**을 채택했습니다.

**Orchestration 방식을 선택한 이유**
- Choreography 방식의 경우 메시징 큐를 사용한 비동기 방식으로 흐름 파악이 쉽지 않음
- 장애 발생 시 어느 단계에서 실패했는지 추적과 복구가 어려움
- Orchestrator가 전체 흐름을 중앙에서 관리하여 트랜잭션 추적 및 디버깅 용이
- 보상 트랜잭션 실행 순서를 명확하게 제어 가능

---

## SAGA 패턴 구현

### 초기 구현: Boolean 기반 상태 관리

처음 SAGA 패턴을 구현할 때 단순하게 접근했습니다. 각 서비스로 분산된 작업의 완료 여부를 Boolean 변수로 관리했습니다.
```kotlin
var pointUsed = false
var reservationConfirmed = false
var seatConfirmed = false

try {
    // 포인트 사용 요청
    pointApiClient.use(
        requestId = requestId,
        userId = userId,
        amount = reservation.price
    )
    pointUsed = true

    // 예약 상태를 PAID로 변경
    concertApiClient.changeReservationPaid(requestId)
    reservationConfirmed = true

    // 좌석 상태를 RESERVED로 변경
    concertApiClient.changeSeatReserved(requestId)
    seatConfirmed = true

    // 결제 정보 저장
    val command = PaymentCreateCommand(
        price = reservation.price,
    )

    val status = paymentService.save(command)

    return PaymentResponse(
        reservationId = reservation.reservationId,
        seatNo = reservation.seatNo,
        status = status,
        price = reservation.price,
    )
} catch (e: Exception) {
    handleSagaRollback(
        pointUsed,
        reservationConfirmed,
        seatConfirmed
    )
    throw ConcertException(ErrorCode.PAYMENT_FAILED)
}
```

실제 테스트 시 각 서비스 간 통신 후 예외 발생 시 보상 트랜잭션을 실행하여 데이터 정합성이 맞춰지는 것을 확인했습니다.

### 초기 구현의 문제점

하지만 위와 같은 방식은 다음과 같은 문제가 있었습니다.

**1. 장애 추적 불가**
- 어떤 서비스에서 장애가 발생했는지 파악 어려움
- 어떤 보상 트랜잭션을 실행해야 하는지 판단 곤란

**2. 복구 불가능**
- 서버 메모리의 변수로만 상태를 관리하다 보니, 결제 서버 자체에 장애 발생 시 복구 불가
- 어디까지 보상 트랜잭션이 완료되었는지, 어디서부터 재시도해야 하는지 알 수 없음

**3. 모니터링 및 디버깅 어려움**
- 트랜잭션 진행 상황을 확인할 방법이 없음
- 장애 원인 분석이 어려움

### 개선 방안: DB 기반 상태 관리

이를 해결하기 위해 SAGA 진행 단계와 실패 지점을 DB에 기록하여 추적, 재시도, 모니터링이 가능하도록 개선했습니다.
```kotlin
try {
    // 포인트 차감
    val pointResponse = sagaExecution.executeStep(
        sagaId,
        POINT_USE
    ) {
        pointApiClient.use(
            userId = userId,
            amount = reservation.price,
        )
    }
    pointHistoryId = pointResponse.pointHistoryId
    
    // 예약 확정
    sagaExecution.executeStep(sagaId, RESERVATION_CONFIRM) {
        concertApiClient.changeReservationPaid(reservationId)
    }

    // 좌석 확정
    sagaExecution.executeStep(sagaId, SEAT_CONFIRM) {
        concertApiClient.changeSeatReserved(reservationId)
    }

    // 결제 저장
    val payment: PaymentEntity = sagaExecution.executeStep(sagaId, PAYMENT_SAVE) {
        paymentService.save(
            PaymentCreateCommand(reservation.price)
        )
    }
    paymentId = payment.id!!

    sagaExecution.completeSaga(sagaId)

    return PaymentResponse(
        reservationId = reservation.reservationId,
        seatNo = reservation.seatNo,
        status = payment.paymentStatus.toString(),
        price = reservation.price,
    )
} catch (e: Exception) {
    handleRollback(
        sagaId = sagaId,
        userId = userId,
        price = reservation.price,
        pointHistoryId = pointHistoryId,
        requestId = reservationId,
        paymentId = paymentId
    )
    throw ConcertException(ErrorCode.PAYMENT_FAILED)
}
```

각 단계를 `executeStep` 메서드로 감싸서 실행 상태를 DB에 기록하도록 개선했습니다.

**개선 효과**

작업 상태 및 실패 지점을 DB에 기록함으로써 다음과 같은 이점을 얻을 수 있습니다.

1. **복구 가능성**: 보상 트랜잭션 동작 중 장애 발생 시, 실패 지점부터 이어서 재시도 가능
2. **추적 가능성**: 어떤 단계에서 실패했는지 명확히 파악 가능
3. **모니터링**: SAGA 진행 상황을 실시간으로 확인 가능

**재시도 전략**

실패한 SAGA는 별도의 배치 또는 스케줄러를 통해 주기적으로 체크하여 자동 재시도합니다.

**재시도 대상**

다음 조건에 해당하는 SAGA를 재시도 대상으로 선정합니다.

1. 실패 지점이 존재하고, 상태가 `FAILED`인 경우
2. 실패 지점이 존재하고, 상태가 `COMPENSATING`이지만 일정 시간(예: 10분) 이상 완료되지 않은 경우
3. 상태가 `IN_PROGRESS`이지만 특정 시간(예: 5분) 이상 진행 중인 경우 (타임아웃)
4. 개발자에게 알림이 나간 `ALERT` 상태가 아닌 경우

**재시도 제한**

- 최대 재시도 횟수: 3회
- 최대 재시도 초과 시 수동 처리 -> 알림으로 개발자가 바로 파악 가능하도록 대응

```kotlin
@DistributedLock(
    key = "saga-retry-lock",
)
@Scheduled(fixedRate = 30000)
fun retryFailSaga() {
    retryFailSagaUseCase.retryFailSagas()
}

@Service
class RetrySagaService (
    private val sagaRepository: SagaRepository,
    private val sagaCompensationStrategyMapper: SagaCompensationStrategyMapper,
): RetryFailSagaUseCase {


    override fun retryFailSagas() {
        val failedSagaList = sagaRepository.getFailedSagas()

        for (saga in failedSagaList) {
            val strategy = sagaCompensationStrategyMapper.getStrategy(saga.sagaType)
            strategy.compensate(saga)
        }
    }
}

override fun compensate(saga: SagaEntity) {
    if (saga.payload == null) {
        log.warn("Saga payload가 존재하지 않습니다")
        return
    }

    val retryAvailable = saga.isRetryAvailable()

    if(!retryAvailable) {
        // 개발자에게 알림 발송
        sendAlert(saga)
        return
    }

    val payload: PaymentCompensation = JsonUtil.decodeFromJson<PaymentCompensation>(saga.payload!!)
    
    val completedSteps = saga.getCompletedStepList()

    var allSuccess = true

    completedSteps
        .reversed()
        .forEach { step ->
        try {
            when (step) {
                POINT_USE -> pointApiClient.cancel(userId, pointHistoryId, price)
                RESERVATION_CONFIRM -> concertApiClient.changeReservationPending(requestId)
                SEAT_CONFIRM -> concertApiClient.changeSeatTemporarilyAssigned(requestId)
                PAYMENT_SAVE -> paymentService.cancelPayment(paymentId)
            }
        } catch (e: Exception) {
            allSuccess = false
            log.error("보상실패: $step - $e")
        }
    }

    checkStepCompleted(saga, allSuccess)

    sagaRepository.save(saga)
}
```

위와 같이 스케줄러를 통해 주기적으로 실패한 SAGA를 조회하고, 보상 트랜잭션을 재시도합니다.

하지만 재시도 전략도 결국 실패할수 있고 최대 재시도 횟수가 3회이므로 3회가 된 시점에는 개발자가 바로 파악할 수 있도록 알림을 보내서 수동으로 처리할 수 있도록 해야 합니다.

## 멱등성 문제

현재 로직으로도 동작에서 단 1번의 재시도 과정에서 예외 없이 재시도 처리가 된 경우 문제가없었지만, 여러번의 재시도 과정이 되어야 하는 경우에는 문제가 발생했습니다.

각 보상 Step이 여러 번 실행되어도 결과가 동일해야 하는 **멱등성(Idempotency)** 이 보장되지 않았기 때문입니다.

**시나리오:**

1. 재시도 보상 트랜잭션 1차 시도
   - POINT_USE 보상 성공 (포인트 1000원 충전)
   - SEAT_CONFIRM 보상 실패
2. 재시도 보상 트랜잭션 2차 재시도
   - POINT_USE 보상 **재실행** (포인트 1000원 추가 충전) ❌
   - SEAT_CONFIRM 보상 성공

**결과:** 포인트가 중복으로 충전되어 사용자에게 2000원이 지급됨

`sagaId`를 멱등성 키(Idempotency Key)로 사용하여 동일한 보상 요청이 중복 실행되는 것을 방지하도록 했습니다.

1. 이미 처리된 요청이지 확인하기 위한 테이블 추가

```kotlin
@Entity
@Table(
    indexes = [Index(name = "idx_saga_id", columnList = "sagaId", unique = true)]
)
class IdempotencyEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val sagaId: String,
): BaseEntity()
```

2. 재시도 처리되는 요청 제일 처음 로직으로 이미 처리된 요청인지 확인
3. 처음 요청의 경우 다음 요청의 멱등성 체크를 위해 ID 저장

```kotlin
@Transactional
override fun cancelPayment(paymentId: Long, sagaId: String) {
    // 멱등성 체크
    val idempotency = idempotencyRepository.findBySagaId(sagaId)
    if (idempotency != null) {
        log.info("이미 요청된 결제 취소 상태 변경 요청입니다 sagaId=$sagaId")
        return
    }

    val payment = paymentRepository.findById(paymentId)
        .orElseThrow { throw ConcertException(ErrorCode.PAYMENT_NOT_FOUND) }

    payment.cancel()

    // 다음 요청을 위해 요청 저장
    idempotencyRepository.save(
        IdempotencyEntity(
            sagaId = sagaId,
        )
    )
}
```

멱등성을 보장하게 수정함으로써 동일한 보상 요청이 여러 번 실행되어도 결과가 동일하게 수행되도록 개선했습니다.

## 마무리

SAGA 패턴 도입과 재시도 전략 구축을 통해 다음과 같은 개선을 이루었습니다.

**분산 트랜잭션 관리**
- MSA 환경에서 여러 서비스에 걸친 트랜잭션의 최종적 일관성 보장
- 장애 발생 시 자동 보상 트랜잭션 실행으로 데이터 정합성 유지

**복구 가능성**
- DB 기반 상태 관리로 서버 장애 시에도 진행 상황 추적 가능
- 실패 지점부터 자동 재시도하여 일시적 장애에 대응