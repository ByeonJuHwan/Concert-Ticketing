# 서비스 규모에 따른 설계

현재 콘서트 좌석 예약의 트랜잭션 범위에 대해 알아보고 서비스 규모가 확장된다면 서비스들을 어떻게 분리하고,
분리에 따른 트랜잭션 처리의 한계와 해결방안에대해서 알아보겠습니다.

## 트랜잭션 범위에 따른 문제점

현재 콘서트 좌석 예약 로직에는 아래와 같습니다.
```text
--- 트랜잭션 시작 ---
회원 정보 조회
--- 락 획득 ---
좌석 조회
좌석 상태 검증
좌석 상태 변경
예약 생성
--- 락 반환 ---
--- 트랜잭션 종료 ---
```

예약에 관한 모든 로직이 하나의 트랜잭션에서 동작하므로 좌석 조회가 오래걸리거나, 상태 검증 과정의 소요시간이 오래걸릴 경우 다음과 같은 문제점이 있습니다.
- **DB 커넥션을 오래 지속하는 문제**
- **락 획득 이후 반환까지의 시간이 오래걸리는 문제**
- **비즈니스 로직간의 강한 결합**

트래픽이 적을 경우에는 큰 문제는 없지만 대용량 트래픽이 몰릴 경우 위와 같은 문제는 시스템의 성능 저하를 초래할 수 있습니다.

또 다른 예시를 들어보겠습니다.

콘서트 좌석 예약 기능에 예약한 좌석 정보를 외부 API 로 송신 해주는 기능이 추가된다고 가정해보겠습니다.


```text
기존로직
---
1. 회원 정보 조회
2. 좌석 상태 검증
3. 좌석 상태 변경
4. 예약 생성
---
기능추가
5. 예약정보 외부 API 송신
```

외부 API로 예약 정보를 송신하는 기능이 추가되면서, 해당 기능이 약 3,4초 소요된다면 전체 트랜잭션 시간이 약 4,5초로 길어집니다. 외부 API 송신은 본래의 비즈니스 로직과 직접적으로 연관이 없지만,
단일 트랜잭션에 메인로직과 포함되어 있어 통신이 실패할 경우 **영향을 받지 말아야 할 비즈니스 로직까지 전체 롤백되는 문제가 발생합니다.** 
이러한 문제는 시스템의 성능과 안정성을 저하시킬 수 있습니다.

> 정리해보자면 데이터 정합성을 보장하기 위해 트랜잭션을 사용하지만, 
트랜잭션의 범위가 길어질수록 성능 저하와 시스템 병목 등의 추가 문제가 발생할 수 있습니다.

## MSA 전환

현재 좌석 예약 로직은 메인 로직과 여러 부가 로직이 결합되어 복잡성을 증가시키고 있습니다. 

메인 로직인 좌석 예약 외에도, 회원 검증, 좌석 검증, 외부 API 송신과 같은 부가 로직이 추가되면서, 
모든 로직이 하나의 트랜잭션 안에서 실행됩니다. 이로 인해 서비스가 무거워지고, 
유지보수가 어려워지는 문제가 발생하고 있습니다. 

이러한 문제를 해결하기 위해, 각 기능을 독립적인 서비스로 분리하는 MSA 를 고려해 볼 수 있습니다.

### 현 시스템의 문제점

- 비대해진 트랜잭션: 좌석 예약과 관련된 모든 로직이 하나의 트랜잭션 내에서 실행되므로, 트랜잭션의 길이가 증가하고 시스템 성능이 저하됩니다.
- 결합된 비즈니스 로직: 모든 비즈니스 로직이 단일 트랜잭션 내에 결합되어 있어, 특정 로직 변경 시 전체 시스템에 영향을 미칩니다.

#### 서비스 분리

1. User Service
	- 기능: 사용자 정보 관리 및 인증
    - 책임: 사용자 정보 조회, 사용자 상태 검증
2. Seat Service
	- 기능: 좌석 상태 관리
	- 책임: 좌석 상태 조회 및 검증, 좌석 상태 변경
3. Reservation Service
    - 기능: 예약 생성 및 관리
    - 책임: 예약 생성, 예약 상태 관리

각 서비스간의 통신은 이벤트 기반으로 `Kafka`, `RabbitMQ` 등 메시지 브로커를 통해서 통신합니다.

#### 트랜잭션 처리의 한계

각 서비스가 독립적으로 작동하므로, 전통적인 ACID 트랜잭션을 유지하기가 어렵고, 데이터 베이스도 각 서비스별로 존재하므로 
데이터 일관성 관리가 어려워집니다.

예를들면, 예약 생성시에 예외가 발생해 로직이 실패한다면 이미 커밋된 좌석 상태를 롤백시키는 동작이 필요해집니다.

#### 해결 방법

SAGA 패턴을 통해서 비즈니스 로직이 실패시 이를 롤백시키는 보상트랜잭션을 통해서 데이터 일관성을 유지할 수 있습니다.

```kotlin
@EventListener
fun handleSeatReserved(event: SeatReservedEvent) {
    try {
        reservationRepository.save(reservation) // 예약생성중 예외발생!!!
    } catch (ex: Exception) {
        // 보상 트랜잭션을 통해 좌석 상태 변경 롤백
        eventPublisher.publishEvent(ReservationCreationFailedEvent(event.seatId))
    }
}



@EventListener
fun handleReservationCreationFailed(event: ReservationCreationFailedEvent) {
    try {
        // 좌석 상태 롤백
        val seat = seatRepository.findById(event.seatId)
            ?: throw SeatNotFoundException("Seat not found: ${event.seatId}")

        if (seat.status == SeatStatus.RESERVED) {
            seat.status = SeatStatus.AVAILABLE
            seatRepository.save(seat)
            log.info("좌석 상태 롤백 성공: ${event.seatId}")
        }
    } catch (ex: Exception) {
        log.error("좌석 상태 롤백 실패: ${event.seatId}", ex)
        // 추가적인 실패 처리 로직 (알림, 재시도 등)
    }
}
```

MSA 로 각 서비스별로 나눔으로써 각 로직에서 필요한 관심사만 비즈니스 로직에 담을 수 있으며, 이를 통해 트랜잭션 범위를 좁게 가져감으로써 쓰레드 풀에서 사용된 쓰레드를 더 빠르게 순환시킬 수 있습니다.

그러나 서비스를 나눔으로 인해 트랜잭션의 ACID 속성을 관리하는 것이 어려워지고, 데이터베이스의 데이터 일관성을 유지하는 데에도 추가적인 노력이 필요합니다.

따라서 MSA 로 무조건 전환하기보다는 현재 비즈니스의 상황과 요구사항을 충분히 고려하여 최적의 아키텍처를 선택하는 것이 중요합니다.

## 이벤트 주도 설계

어플리케이션에 EVENT 를 발행 및 구독 하게 비즈니스로직을 구현한다고 하면 어떻게 활용할 수 있을까요?

- 비대해진 트랜잭션 내의 각 작업을 작은 단위의 트랜잭션으로 분리 가능합니다
- 특정 작업이 완료되었을 때, 후속 작업이 이벤트에 의해 Trigger 되도록 구성함으로써 과도하게 많은 비즈니스 로직을 알고 있을 필요가 없습니다
- 트랜잭션 내에서 외부 API 호출의 실패나 수행이 주요 비즈니스 로직에 영향을 주지 않도록 구성할 수 있습니다

그러면 이제 스프링 환경에서 이벤트를 발행함으로써 각 서비스 간의 의존도를 낮추는 방법과 작업을
외부 API 통신 이라는 기능을 추가하면서 구현해 보겠습니다.

### EventPublisher

스프링에서는 `ApplicationEventPublisher` 를 사용해서 이벤트를 발행할 수 있습니다.

이벤트로는 `ReservationSuccessEvent` 를 만들어 리스너쪽에서 이벤트를 받을수 있도록 합니다.

```kotlin
@Component
class ReservationEventPublisherImpl (
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ReservationEventPublisher{

    private val log : Logger = LoggerFactory.getLogger(ReservationEventPublisherImpl::class.java)

    override fun publish(event: ReservationSuccessEvent) {
        applicationEventPublisher.publishEvent(event)
        log.info("예약 성공 이벤트 발행")
    }
}
```

### EventListener / TransactionalEventListener

이벤트를 수신하는 방법으로는 두 가지 어노테이션이 있습니다.

`@EventListener` 는 스프링에서 이벤트를 수신하고 처리하는 기본 어노테이션으로, 트랜잭션과 무관하게 이벤트를 처리합니다.

`@TransactionalEventListener` 는 Publisher 의 트랜잭션의 상태에 따라 이벤트를 처리하며, 지정된 트랜잭션 경계 (커밋,롤백) 에 따라 이벤트 핸들러를 실행합니다.

`@TransactionalEventListener` 의 트랜잭션별 이벤트 처리 옵션에는 아래 4개의 옵션이 있습니다.

- `BEFORE_COMMIT` : 트랜잭션이 커밋되기 전에 이벤트를 처리합니다.
- `AFTER_COMMIT` : 트랜잭션이 성공적으로 커밋된 후에 이벤트를 처리합니다.
- `AFTER_ROLLBACK` : 트랜잭션이 롤백된 후에 이벤트를 처리합니다.
- `AFTER_COMPLETION` : 트랜잭션이 완료된 후(성공이든 실패든) 에 이벤트를 처리합니다. -> `finally` 와 유사하게 동작

아래 순서는 `@TransactionalEventListener` 와 `@EventListener` 가 이벤트를 받았을때 각 옵션에 따른 동작 방식 입니다.

```text
1. 이벤트 발행
2. EventListener 중 처리가능한게 있으면, 돌면서 실행시킴
3. 커밋 직전 ( 위에 다 성공했음 )
4. BEFORE_COMMIT 중 처리가능한게 있으면, 돌면서 실행시킴
5. 커밋
6. AFTER_COMMIT 중 처리가능한게 있으면, 돌면서 실행시킴
7. 트랜잭션 종료
8. AFTER_COMPLETION 중 처리가능한게 있으면, 돌면서 실행시킴

예외 발생
1. 롤백
2. AFTER_ROLLBACK 중 처리가능한게 있으면서, 돌면서 실행시킴
3. 트랜잭션 종료
4. AFTER_COMPLETION 중 처리가능한게 있으면, 돌면서 실행시킴
```

그렇다면 어떤 어노테이션을 사용하고 어떤 옵션을 사용해야 기존 비즈니스로직에 영향받지 않으면서 요구사항을 만족할 수 있을까요?

예약 정보를 보내기 위해서는 예약 정보가 먼저 생성되어야 합니다. 따라서 `@EventListener` 와 `BEFORE_COMMIT` 을 사용할 수 없습니다.
또한, 예약 생성 시 예외가 발생하면 예약 데이터를 보내서는 안 되므로, 로직 실패와 상관없이 동작하는 `AFTER_COMPLETION` 도 적절하지 않습니다.

**따라서 저는 `@TransactionalEventListener` 의 `AFTER_COMMIT` 을 사용하여 구현했습니다.**

그리고 외부 API 로는 예약정보를 슬랙으로 보내도록 구현했습니다.

```kotlin
@Component
class ReservationEventListener (
    private val dataPlatformFacade: DataPlatformFacade,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleExternalApiEvent(event: ReservationSuccessEvent) {
        dataPlatformFacade.sendReservationData(event.reservationId)
    }
}
```

```kotlin
@Service
class DataPlatformServiceImpl (
    private val messageManager: MessageManager
) : DataPlatformService {

    private val log : Logger = LoggerFactory.getLogger(DataPlatformServiceImpl::class.java)

    override fun sendReservationData(reservation: ReservationEntity) {
        messageManager.sendMessage("예약 데이터 전송 reservationId : ${reservation.id}")
        log.info("외부 API 통신 성공")
    }
}
```

```kotlin
@Component
class SlackApiClient : MessageManager {

    @Value("\${slack.token}")
    lateinit var token : String

    @Value("\${slack.channel.id}")
    lateinit var channelId : String

    private val log : Logger = LoggerFactory.getLogger(SlackApiClient::class.java)

    override fun sendMessage(message : String) {
        val client = Slack.getInstance().methods()

        client.chatPostMessage {
            it.token(token)
                .channel(channelId)
                .text(message)
        }

        log.info("슬랙 메세지 전송 완료")
    }
}
```

![](https://velog.velcdn.com/images/asdcz11/post/69e004ec-03cc-4dbf-afac-b37908e41124/image.png)

---

**이제 실제 리스너에서 의도적으로 예외를 발생시켜서 외부 API 와의 통신이 실패해도 예약 API 에는 영향이 안가는지 확인해 보겠습니다.**

```kotlin
@Service
class DataPlatformServiceImpl (
    private val messageManager: MessageManager
) : DataPlatformService {

    private val log : Logger = LoggerFactory.getLogger(DataPlatformServiceImpl::class.java)

    override fun sendReservationData(reservation: ReservationEntity) {
        throw RuntimeException("예외가 발생해도 기존로직에는 영향이 안가도록 이벤트 처리")
    }
}
```

로그를 보면 의도대로 `RuntimeException` 이 발생했습니다

![](https://velog.velcdn.com/images/asdcz11/post/8accc8fc-7255-4319-8108-81979e4eb0e2/image.png)

그러나 실제 사용자의 응답에서는 예약이 정상적으로 완료된 것을 확인할 수 있습니다. 
이는 이벤트 로직이 비동기로 실행되기 때문에, 기존 메인 스레드의 로직에 영향을 주지 않고 정상 응답을 반환할 수 있기 때문입니다.

**따라서, 이벤트를 수신한 로직에서 실패가 발생하더라도, 기존 로직에는 영향을 미치지 않는다는 것을 확인할 수 있었습니다.**

![](https://velog.velcdn.com/images/asdcz11/post/ac618b50-a2f7-44ed-a692-da6d11f02ba3/image.png)

### `@TransactionalEventListener` 과 트랜잭션 연관성

만약 `@TransactionalEventListener` 에서 `@Transactional` 이 없다면 어떻게 동작할까요??

**결과는 이벤트 리스터가 동작을 하지 않습니다.** 트랜잭션에 따라 동작하는 방식이 다르기 때문에 트랜잭션이 없으면 이벤트 리스터가 정상적으로 동작하지 않습니다.

### `@EventListener`와 `@TransactionalEventListener` 의 관계

또 다른 질문으로 `@EventListener` 와 `@TransactionalEventListener` 를 같이 사용하면 몇 번 동작할까요??

**결과는 1번만 동작합니다** `@EventListener` 와 `@TransactionalEventListener` 는 둘 다 이벤트를 수신하는 역할을 하지만, 
`@TransactionalEventListener` 는 트랜잭션의 특정 단계에서만 이벤트를 처리하도록 하는 기능을 추가로 제공합니다.

`@TransactionalEventListener` 는 `@EventListener` 의 특성을 포함하면서도 트랜잭션 경계를 추가적으로 설정하는 역할을 하기 때문에, `@EventListener` 는 별도로 작동하지 않습니다.

## 정리하며...

트랜잭션의 범위에 대해 고민해 보고, 비즈니스 로직의 융합으로 인한 문제점과 그 해결 방안에 대해 알아보았습니다.
서비스의 규모가 증가할 때, 이벤트를 통해 각 도메인을 분리함으로써 이러한 문제를 해결할 수 있지만, 이 과정에서도 트랜잭션과 데이터 정합성에 대한 문제가 존재합니다.

따라서 현재 비즈니스를 이해하고 상황에 맞게 적절한 설계와 패턴을 사용해야 할 것 같습니다.