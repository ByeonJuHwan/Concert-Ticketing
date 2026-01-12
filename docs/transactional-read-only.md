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


### 마무리 

위처럼 코드를 변경하고 나니 기존에 의도하지 않게 수행되었던 `set autoCommit`, `Transaction` 쿼리가 사라지고 실제 조회 쿼리만 남는 것을 확인할 수 있었습니다.

이번 기회를 통해서 `@Transactional`의 기본 전파 레벨과 `readOnly` 옵션이 실제 DB 쿼리에 미치는 영향을 알게 되었습니다.

성능 튜닝 관점에서 기존에 아무렇지도 않게 사용하던 기능에서도 성능 저하가 발생할 수 있다는 걸 알게 된 좋은 경험이었습니다.

---

#### 참고

[참고-카카오페이 JPA Transactional 잘 알고 쓰고 계신가요?](https://tech.kakaopay.com/post/jpa-transactional-bri/)