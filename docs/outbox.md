# Kafka 아웃박스 패턴

카프카 아웃박스 패턴은 분산 시스템에서 이벤트 발행의 신뢰성을 보장하기 위한 설계 방식입니다.

이 패턴은 데이터베이스 트랜잭션과 이벤트 발행을 결합하여, 
시스템 장애나 네트워크 오류 발생 시에도 이벤트의 안전한 처리를 보장합니다. 
아웃박스 테이블에 이벤트를 먼저 저장함으로써, 실제 발행에 실패하더라도 재시도를 (배치, 스케줄러등) 통해 이벤트 발행에 대한 신뢰성을 유지할 수 있습니다.

## Kafka 아웃박스 패턴 구현

콘서트 좌석 예약 시스템을 예로 들어 아웃박스 패턴의 구현 방법을 살펴보겠습니다.

### 1. 비즈니스 로직 실행과 이벤트 발행

좌석 예약의 비즈니스 로직이 성공하면 예약 성공 이벤트를 발행합니다.

```kotlin
@Transactional
fun reserveSeat(request: ConcertReservationDto): ConcertReservationResponseDto {
    // 유저 정보 조회
    val user = userService.getUser(request.userId)
    //예약가능인지 확인하고 좌석 임시배정해 잠근다.
    val reservation = reservationService.createSeatReservation(user, request.seatId)
    // 예약 성공 이벤트 발행
    eventPublisher.publish(ReservationSuccessEvent(reservation.id))
    return ConcertReservationResponseDto(
        status = reservation.status,
        reservationExpireTime = reservation.expiresAt
    )
}
```

### 2. 아웃박스 테이블에 이벤트 저장

`@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` 를 사용하여 트랜잭션 커밋 직전에 아웃박스 테이블에 이벤트를 저장합니다.
이 시점에서 이벤트의 상태는 `INIT` 입니다.

이 방식은 비즈니스 로직과 이벤트 저장이 동일한 트랜잭션 내에서 실행되므로, 데이터 일관성을 보장합니다. 즉, outbox 에 이벤트 정보 저장이 실패하면
비즈니스로직도 실패로 봅니다.

왜냐하면 비동기 메시징을 활용한 서비스 구현에서는 비즈니스 로직이 실행되었을 때, 이를 표현하는 이벤트도 온전하게
발행되는 것이 중요하기 때문입니다.


```kotlin
/**
 * [아웃박스 패턴]
 * BEFORE_COMMIT 으로 실행되어 어떤 이벤트가 발행되어야 하는지 저장한다
 */
@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT) 
fun handleReservationOutBox(event: ReservationEvent) {
    log.info("BEFORE_COMMIT : 아웃박스 이벤트 저장")
    reservationFacade.recordReservationOutBoxMsg(event)
}
```
### 3. Kafka 이벤트 발행

트랜잭션이 성공적으로 커밋된 후, `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` 에 의해 Kafka 이벤트가 발행됩니다. 

이 과정은 비동기로 실행되어 비즈니스 로직의 응답, 응답 시간에 영향을 주지 않습니다.

만약 Kafka 이벤트 발행에 실패하면, 해당 이벤트의 상태는 `SEND_FAIL` 로 변경됩니다. 이를 통해 추후 재시도 메커니즘을 통해 실패한 이벤트를 처리할 수 있습니다.

```kotlin
/**
 * [아웃박스 패턴]
 * AFTER_COMMIT 시 카프카 이벤트를 발행한다
 * 만약 카프카 이벤트를 발행했지만 발행과정에서 예외발생시 SEND_FAIL 상태로 변경된다.
 */
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun publishReservationEvent(event: ReservationEvent) {
    // 카프카 이벤트 발행
    runCatching {
        reservationFacade.publishReservationEvent(event)
    }.onFailure { e ->
        log.error("Kafka Message Send Failed!" , e)
        reservationFacade.changeReservationOutBoxStatusSendFail(event.toEntity().reservationId)
    }
}
```

### 4. Kafka 이벤트 Consume

`@KafkaListener`를 사용하여 발행된 이벤트를 처리합니다. 이 단계에서 주목해야 할 두 가지 중요한 점이 있습니다:

1. `아웃박스 이벤트 상태 업데이트` :
   이벤트 처리 시 아웃박스에 저장된 이벤트의 상태를 `SEND_SUCCESS`로 변경합니다.
   이는 이벤트 재처리 로직 실행 시 중복 처리를 방지하는 핵심 단계입니다.

2. `외부 API 연동 및 에러 처리` :
   외부 API 와의 연동 과정에서 발생하는 오류는 아웃박스 패턴의 이벤트 발행 보장과는 별개의 문제입니다.
   따라서, 이러한 오류는 개발자가 즉시 인지하고 대응할 수 있도록 `Slack` 을 통해 알림을 보내도록 구현했습니다.

```kotlin
/**
 * 아웃 박스 패턴 적용
 *
 * 1. 아웃박스 상태를 SEND_SUCCESS 로 상태변경
 * 2. 예약 관련 외부 API 호출 (이로직에서는 Slack)
 */
@Async
@KafkaListener(topics = ["reservation"], groupId = "concert_group")
fun handleExternalApiKafkaEvent(reservationId : String) {
    log.info("Kafka Event 수신 성공!!")
    runCatching {
        reservationFacade.changeReservationOutBoxStatusSendSuccess(reservationId.toLong())
        dataPlatformFacade.sendReservationData(reservationId.toLong())
    }.onFailure { ex ->
        // 아웃박스 패턴으로 발행은 정상적으로 이루어지는게 보장됨
        // 비즈니스로직 혹은 외부 api 문제일 수 있으므로 예외를 슬랙으로 보내도록 처리
        log.error("데이터 플랫폼 전송 에러 : ${ex.message}", ex)
        messageManager.sendMessage("예약 데이터 전송 수신 에러 -> ${ex.message}")
    }
}
```

### 5. 발행 실패한 이벤트 재시도

아웃박스 패턴의 안정성을 더욱 높이기 위해, 발행이 실패한 이벤트를 주기적으로 재시도합니다.

- 대상: 상태가 `SEND_SUCCESS`가 아니면서, `CREATED_AT`이 현재 시점으로부터 10분 이상 경과한 이벤트
- 방법: 스케줄러를 사용하여 10분마다 해당 이벤트들을 10분마다 재발행

이 재시도를 통해 일시적인 네트워크 문제나 시스템 장애로 인해 발행되지 못한 이벤트들을 처리할 수 있습니다.

```kotlin
/**
 * [아웃박스 패턴]
 * 이벤트 재시도 스케줄러
 * 조건: 상태가 SEND_SUCCESS가 아니고, CREATED_AT이 10분 이상 지난 이벤트
 */
@Scheduled(fixedRate = 600000) // 10분(600,000ms)마다 실행
fun reservationEventRetryScheduler() {
    reservationFacade.retryEvents()
}
```

## 정리하며...

이전에는 주로 메시지 수신과 처리 즉 비즈니스 로직 처리에 초점을 맞추었지만, 이번 경험을 통해 이벤트 발행 과정에서 발생할 수 있는 문제 상황과
그 해결 방법에 대해 고민하고 구현해 볼 수 있어서 좋은 경험이었습니다.

---

**참조문서**

[트랜잭셔널 아웃박스 패턴의 실제 구현 사례 - 29CM](https://medium.com/@greg.shiny82/%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%94%EB%84%90-%EC%95%84%EC%9B%83%EB%B0%95%EC%8A%A4-%ED%8C%A8%ED%84%B4%EC%9D%98-%EC%8B%A4%EC%A0%9C-%EA%B5%AC%ED%98%84-%EC%82%AC%EB%A1%80-29cm-0f822fc23edb)