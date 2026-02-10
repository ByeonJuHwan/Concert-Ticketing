# Transactional Read Only 성능개선

스프링 개발을 하거나 백엔드 개발자라면 당연시하게 사용해오던 `@Transactional`을 잘못 사용하면 성능 저하가 발생할 수 있습니다.

`@Transactional(readOnly = true)`를 설정하면 읽기 전용 트랜잭션으로 설정되어 JPA에서 변경 감지를 하지 않아 애플리케이션 성능이 개선됩니다.

하지만 DB 쿼리를 확인해본 결과 예상과 다른 동작을 발견했고, 어떻게 해결했는지 정리해보았습니다.

---

### 문제 상황

#### 예시 코드

선택한 콘서트의 예약가능한 날짜정보를 조회하는 코드입니다

읽기 기능을 수행하기 때문에 `@Transactional(readOnly = true)` 를 사용했습니다

```kotlin
@Transactional(readOnly = true)
override fun getAvailableDates(concertId: Long): List<ConcertDateInfo> {
    val concertsDates = concertReadRepository.getAvailableDates(concertId)
    return concertsDates.map { ConcertDateInfo.from(it) }
}
```

```sql
SELECT c.id, c.concert_name, co.concert_date, co.concert_time
  FROM concert c
 INNER JOIN concert_option co ON c.id = co.concert_id
 WHERE c.id = ?
```

실제 Info 로그나 DB에서 수행될거라고 생각하는 쿼리는 위 SELECT 문 한 줄입니다.

DB로깅 설정을 변경하고 실제 위 API 를 실행해본 결과 수행된 쿼리는 아래와 같았습니다.

```sql
2026-01-06T13:14:07.800+09:00 | connection | SET autocommit=0 | 0ms
2026-01-06T13:14:07.801+09:00 | statement  | SET SESSION TRANSACTION READ ONLY | 0ms
2026-01-06T13:14:07.811+09:00 | statement  | 
SELECT c.id, c.concert_name, co.concert_date
  FROM concert c
 INNER JOIN concert_option co ON c.id = co.concert_id
 WHERE c.id = 1
| 10ms
2026-01-06T13:14:07.839+09:00 | commit     | COMMIT | 1ms
2026-01-06T13:14:07.840+09:00 | connection | SET autocommit=1 | 0ms

총 5개의 DB 명령 실행
```

예상과 달리 트랜잭션 관리를 위한 4개의 추가 쿼리가 실행되었습니다.

---

### 원인 분석

#### 트랜잭션 기본 전파 레벨

Spring의 @Transactional 기본 전파 레벨은 REQUIRED입니다.

```kotlin
@Transactional(propagation = Propagation.REQUIRED)
```

REQUIRED의 동작 방식은 아래와 같습니다

- 기존 트랜잭션 있음 → 참여
- 기존 트랜잭션 없음 → 새로 생성

단순 조회인데도 트랜잭션이 새로 생성되어 불필요한 오버헤드가 발생합니다.

#### 트랜잭션 생성 시 발생하는 오버헤드

```text
트랜잭션이 생성되는 경우 (REQUIRED):
1. 커넥션 풀에서 커넥션 획득
2. SET autocommit=0 실행
3. SET SESSION TRANSACTION READ ONLY 실행 (readOnly=true인 경우)
4. 실제 SELECT 쿼리 실행
5. COMMIT 실행
6. SET autocommit=1 실행
7. 커넥션을 풀에 반환

→ 단순 조회에 7단계의 작업 수행
```

실제 참고 자료에서 k6 로 위 쿼리들이 성능에 영향을 미치는지 테스트해본결과는 다음과 같았습니다

- API 응답 시간: 약 2배 향상
- DB 처리 성능: 약 2~3배 향상
- 커넥션 사용 시간: 약 50% 감소 

---

### 해결 방법

#### 전파 레벨 변경: SUPPORTS 사용

`propagation` 중 여러 설정 값이 있지만 이중 `SUPPORTS` 를 붙여서 사용하는걸로 했습니다.

`supports` 의 경우 기존 트랜잭션이 있다면 참여하고, 기존 트랜잭션이 없다면 트랜잭션 없이 실행됩니다

- 기존 트랜잭션 있음 → 참여
- 기존 트랜잭션 없음 → 트랜잭션 없이 실행

```kotlin
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
annotation class ReadOnlyTransactional
```

저는 참고자료에서 사용한대로 별도의 커스텀 어노테이션을 만들어서 사용했습니다.

#### findById 의 경우 별도 queryDsl로 분리

Spring Data JPA의 `SimpleJpaRepository` 는 클래스 레벨에 `@Transactional(readOnly = true)`가 이미 적용되어 있습니다.

그래서 이미 많은 서비스계층에서 많이 사용하는 `findById` 의 경우 별도의 레포지토리에서 `queryDsl` 로 작성하여 의도대로 동작하도록 했습니다

```kotlin
interface UserQueryRepository {
    fun findById(id: Long): Optional<UserEntity>
}

@Component
class UserQueryRepositoryImpl (
    private val queryFactory: JPAQueryFactory,
): UserQueryRepository {

    @ReadOnlyTransactional
    override fun findById(id: Long): Optional<UserEntity> {
        val user = QUserEntity.userEntity

        return Optional.ofNullable(queryFactory.selectFrom(user).where(user.id.eq(id)).fetchOne())
    }
}
```

---

### 개선결과

Before

```text
2026-01-06 13:14:07.800 | connection | SET autocommit=0 | 0ms
2026-01-06 13:14:07.801 | statement  | SET SESSION TRANSACTION READ ONLY | 0ms
2026-01-06 13:14:07.811 | statement  | 
SELECT c.id, c.concert_name, co.concert_date
  FROM concert c
 INNER JOIN concert_option co ON c.id = co.concert_id
 WHERE c.id = 1
| 10ms
2026-01-06 13:14:07.839 | commit     | COMMIT | 1ms
2026-01-06 13:14:07.840 | connection | SET autocommit=1 | 0ms

총 5개의 DB 명령 실행
```

After 

```text
2026-01-06 13:20:15.123 | statement  | 
SELECT c.id, c.concert_name, co.concert_date
  FROM concert c
 INNER JOIN concert_option co ON c.id = co.concert_id
 WHERE c.id = 1
| 8ms

총 1개의 DB 명령 실행 (80% 감소)
```

---

### 주의사항: SUPPORTS 적용 시 알아야 할 점

성능이 개선되는 건 확인했지만, 모든 조회에 SUPPORTS를 적용하면 안 됩니다.

SUPPORTS는 트랜잭션을 열지 않기 때문에 MySQL InnoDB의 MVCC(Multi-Version Concurrency Control)가 동작하지 않습니다.

MVCC는 트랜잭션이 시작될 때 Read View(스냅샷)를 생성하고, 이후 모든 SELECT는 이 스냅샷 기준으로 데이터를 읽습니다.
하지만 트랜잭션이 없으면 각 SELECT가 독립된 auto-commit 트랜잭션으로 실행되어 매번 새로운 Read View가 만들어집니다.

이게 왜 문제가 되냐면, 하나의 메서드 안에서 같은 데이터를 두 번 읽는 경우를 생각해보면 됩니다.

#### Non-Repeatable Read 예시
```kotlin
@ReadOnlyTransactional  // SUPPORTS → 트랜잭션 없음
fun checkConcertPrice(concertId: Long) {
    val name1 = concertRepository.findName(concertId)  // 아이유 콘서트
    // 이 사이에 관리자가 이름을 방탄소년단 콘서트로 변경 -> 다른 스레드
    val name2 = concertRepository.findName(concertId)   // 방탄소년단 콘서트
    // 같은 메서드인데 값이 달라짐
}
```

트랜잭션이 있었다면 MVCC 스냅샷 덕분에 name2도 아이유 콘서트로 읽혔을 겁니다.

#### 여러 테이블 조인시

여러테이블 조인시에도 트랜잭션이 존재하지 않으므로 중간에 값이 변동되면 값이 변경되게 됩니다
간단하게 주문로직에서 아래와 같은 문제가 발생합니다.

```kotlin
@ReadOnlyTransactional  // SUPPORTS → 트랜잭션 없음
fun getOrderSummary(orderId: Long): OrderSummaryDto {
    val order = orderRepository.findById(orderId)       // totalAmount = 30,000원
    // 이 사이에 사용자가 주문 1개 취소! totalAmount → 20,000원
    val items = orderItemRepository.findByOrderId(orderId)  // 2개, 합계 20,000원
    // 총액은 30,000원인데 상세 합계는 20,000원 → 불일치
}
```

위와 같은 치명적인 문제점이 존재하기 때문에 이번 프로젝트에서는 `findById`, `findByConcertTime` 같이 다른 테이블과 조인이 없고, 바로 값이 리턴되는 간단한 비드니스 로직에만 적용했습니다.

---


### 마무리 

위처럼 코드를 변경하고 나니 기존에 의도하지 않게 수행되었던 `set autoCommit`, `Transaction` 쿼리가 사라지고 실제 조회 쿼리만 남는 것을 확인할 수 있었습니다.

이번 기회를 통해서 `@Transactional`의 기본 전파 레벨과 `readOnly` 옵션이 실제 DB 쿼리에 미치는 영향을 알게 되었습니다.

성능 튜닝 관점에서 기존에 아무렇지도 않게 사용하던 기능에서도 성능 저하가 발생할 수 있다는 걸 알게 된 좋은 경험이었습니다.

---

#### 참고

[참고-카카오페이 JPA Transactional 잘 알고 쓰고 계신가요?](https://tech.kakaopay.com/post/jpa-transactional-bri/)