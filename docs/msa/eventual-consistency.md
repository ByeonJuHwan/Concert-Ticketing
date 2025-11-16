## 데이터 중복을 통한 서비스 간 결합도 감소

MSA 환경에서 사용자 정보는 여러 서비스에서 빈번하게 참조됩니다.

- Concert-Service: 예약 시 사용자 정보 확인
- Payment-Service: 결제 시 사용자 검증
- Token-Service: 대기열 등록 시 사용자 식별

### 문제 상황

기존 `Monolithic` 설계에서는 하나의 DB에서 모든 도메인 정보를 관리했기 때문에, 
쿼리의 Join문을 사용하거나 별도의 User 정보 조회 등을 통해 각 도메인에서 빈번하게 사용되는 데이터에 접근하는 것이 자유로웠습니다.

하지만 MSA 환경에서는 작은 단위의 서버로 나뉘고, DB도 도메인별로 분리됩니다. 
그 결과 예약, 대기열 등 사용자 조회가 필요한 로직에서 직접적인 User 데이터 접근이 불가능해졌고, 
HTTP 요청을 통해 `User-Service`에서 필요한 데이터를 매번 요청해야 하는 상황이 발생했습니다.


```kotlin
fun 예약() {
    // User-Service HTTP 요청 
    // -> User-Service에서 응답이 10초 이상 지연되거나 장애가 발생한다면..?
    
    // 좌석 임시 예약
    
    // 예약 데이터 생성
    
    // 예약 데이터 생성 이벤트 발행
}
```

**동기 HTTP 호출의 문제점**
- `User-Service` 장애 시 다른 서비스도 영향을 받음
- 네트워크 레이턴시 누적 (연쇄 호출)
- `User-Service`의 부하 증가


### 해결 방안: 이벤트 기반 데이터 복제

이를 해결하기 위해 사용자 회원가입 시점에 사용자 생성 이벤트를 발행하여 `Kafka`를 통해 
다른 서비스로 사용자 데이터를 전송하는 방식을 적용했습니다.

각 서비스는 자체 DB에 필요한 사용자 정보를 저장하여, `User-Service`에 HTTP 요청 없이 로컬에서 데이터를 조회할 수 있게 되었습니다.

### Kafka를 활용한 최종적 일관성(Eventual Consistency) 구현

사용자 데이터는 여러 서비스에서 자주 참조하는 중요한 데이터이므로, 
이벤트 발행을 보장하는 **Outbox Pattern**을 적용하여 데이터는 생성되었지만 이벤트 발행이 실패하는 상황을 방지했습니다.

#### 1. User-Service: 사용자 생성 및 이벤트 발행
```kotlin
@Transactional
override fun createUser(command: CreateUserCommand) {
    // 1. 유저 엔티티 생성
    val user = UserEntity(
        name = command.name,
    )
    
    // 2. 유저 데이터 저장
    val savedUser = userWriteRepository.save(user)

    // 3. 유저 생성 이벤트 생성
    val userCreatedEvent = UserCreatedEvent(
        userId = savedUser.id!!.toString(),
        userName = savedUser.name,
    )
    
    // 4. 유저 생성 이벤트 발행 (Outbox Pattern)
    eventPublisher.publish(userCreatedEvent)
}
```

Outbox Pattern에 의해 트랜잭션 커밋 전 `BEFORE_COMMIT` 단계에서 Outbox 테이블에 이벤트가 저장되고, 
`AFTER_COMMIT` 이후 Kafka로 이벤트가 발행됩니다. 이를 통해 데이터 저장과 이벤트 발행의 원자성을 보장합니다.

#### 2. Concert-Service: 이벤트 수신 및 데이터 저장

발행된 이벤트를 각 서비스에서 구독하여 도메인에 맞게 가공 후 로컬 DB에 저장합니다.

```kotlin
@KafkaListener(
    topics = ["user.create"],
    groupId = "user-create-group",
    concurrency = "3",
)
fun userCreatedConsumer(eventString: String) {
    try {
        val event = JsonUtil.decodeFromJson(eventString)
        concertUserCreateUseCase.createUser(event)
    } catch (e: Exception) {
        log.error("이벤트 처리 실패: {}", eventString, e)
    }
}
```

### 효과

이제 각 서비스가 자체 DB에 사용자 데이터를 보유하게 되어, HTTP 요청 없이 직접 쿼리로 데이터를 조회할 수 있습니다.

**개선된 점**
- `User-Service` 장애 시에도 다른 서비스는 정상 동작
- 네트워크 호출 제거로 응답 시간 단축
- `User-Service`의 부하 감소

**트레이드오프**
- 최종적 일관성: 이벤트 발행부터 각 서비스에 반영까지 수 ms ~ 수 초의 지연 발생 가능
- 저장 공간: 사용자 데이터가 여러 서비스의 DB에 중복 저장
- 복잡도 증가: Outbox Pattern, Kafka 인프라 관리 필요