# DB의 인덱스란??
데이터베이스의 인덱스는 테이블 내의 데이터를 빠르게 검색할 수 있도록 도와주는 자료구조로 익히 알려져 있습니다.
인덱스를 사용하면 데이터베이스는 전체 테이블을 스캔하지 않고도 원하는 데이터를 효율적으로 찾아낼 수 있습니다.

이제부터 콘서트 좌석 예약 시스템에서 인덱스를 적용하여야할 쿼리는 어떤 부분이며 어떻게 적용해야할지 알아보겠습니다.

## 인덱스를 적용해야할 쿼리

콘서트 좌석 예약 시스템에서 조회쿼리를 사용하는 부분은 크게 다음과 같습니다.

1. 예약가능한 콘서트 목록 조회
2. 예약가능한 콘서트 날짜 조회
3. 예약가능한 콘서트 좌석 조회
4. 예약만료시간이 지난 예약 조회 (스케줄러)

## JPA 인덱스 적용 방법

JPA에서는 `@Table` 어노테이션의 `indexes` 속성을 사용하여 엔티티 클래스에 인덱스를 추가할 수 있습니다. 이 방법은 JPA 가 테이블 생성 시 인덱스도 같이 자동으로 생성됩니다.

```kotlin
@Table(name = "concert" , indexes = [Index(name = "idx_start_date", columnList = "startDate")])
```

`@Table` 어노테이션의 indexes 속성을 사용해 `startDate` 컬럼에 `idx_start_date` 이라는 이름의 인덱스를 추가했습니다. columnList 속성은 인덱스를 적용할 컬럼의 이름을 지정합니다.

## 인덱스 (미적용 / 적용) 테스트

콘서트 목록 조회와 예약 만료 시간이 지난 예약 조회 쿼리는 모두 WHERE 절에서 부등호 (>) 조건을 사용합니다. 따라서 콘서트 목록 조회 쿼리를 기준으로 인덱스 성능에 대한 전후 비교를 수행하겠습니다.

### 예약가능한 콘서트 목록 조회

약 10만개의 콘서트 데이터를 미리 생성 후 인덱스를 사용한 쿼리와 사용하지 않은 쿼리의 속도를 비교해보겠습니다.

EXPLAIN 을 SELECT 쿼리 앞에 사용하면 WHERE 절을 사용해서 검색하는지 아니면 인덱스를 사용하는지 확인할 수 있습니다.

인덱스를 `start_date` 컬럼에 적용하여 쿼리를 실행해보겠습니다.

```sql
explain select
    ce1_0.id,
    ce1_0.concert_name,
    ce1_0.end_date,
    ce1_0.reserve_end_date,
    ce1_0.reserve_start_date,
    ce1_0.singer,
    ce1_0.start_date
from
    concert ce1_0
where
    ce1_0.start_date>=?;
```

#### 인덱스 적용전 Explain 쿼리 결과와 쿼리 시간 계산 결과
![](https://velog.velcdn.com/images/asdcz11/post/2676dbcd-3517-424d-99ad-fec166a5d87b/image.png)

| Field         | Value    |
|---------------|----------|
| id            | 1        |
| select_type   | SIMPLE   |
| table         | ce1_0    |
| type          | ALL      |
| possible_keys | <null>   |
| key           | <null>   |
| key_len       | <null>   |
| ref           | <null>   |
| rows          | 98031    |
| Extra         | Using where |

**전체 테이블 스캔**: `type` 필드가 `ALL`로 설정되어 있어, 이 쿼리는 전체 테이블 스캔을 수행하고 있습니다. 이는 인덱스가 적용되지 않았다고 볼 수 있습니다.

**인덱스 미사용**: `key` 필드가 `null` 이므로, 어떠한 인덱스도 사용되지 않고 있습니다.

![](https://velog.velcdn.com/images/asdcz11/post/0478f21c-6a8c-4e83-a5fb-659eeaf71169/image.png)

7097 개의 콘서트 결과를 조회하는데 132 ms 가 소요되었습니다.

이제 같은 데이터에 인덱스를 적용한 후 쿼리를 실행해보겠습니다.

---

#### 인덱스 적용후 Explain 쿼리 결과와 쿼리 시간 계산 결과

![](https://velog.velcdn.com/images/asdcz11/post/9d41738a-96e3-49ec-8cf2-f7ec3db4d061/image.png)

| Field         | Value             |
|---------------|-------------------|
| id            | 1                 |
| select_type   | SIMPLE            |
| table         | concert           |
| type          | range             |
| possible_keys | idx_start_date    |
| key           | idx_start_date    |
| key_len       | 1023              |
| ref           | <null>            |
| rows          | 14192             |
| Extra         | Using index condition |


- **인덱스 사용**: `type` 필드가 `range`로 설정되어 있어, 이 쿼리는 인덱스를 범위 검색에 사용하고 있습니다. 이는 인덱스가 효과적으로 사용되었다는 것을 나타냅니다.
- **인덱스 유형**: `key` 필드에 `idx_start_date` 인덱스가 사용되고 있으며, `possible_keys`에도 동일한 인덱스가 나타납니다. 이는 해당 인덱스가 쿼리 조건을 만족하는 레코드를 찾는 데 유용하다는 것을 의미합니다.
- **효율적인 행 필터링**: `rows` 값이 14192로 나타나, 이 인덱스가 결과값을 검색할때 인덱스를 추가하기 전보다 상대적으로 적은 수의 행을 스캔함으로써 성능을 최적화하고 있습니다.

![](https://velog.velcdn.com/images/asdcz11/post/8c016853-3fe7-45f1-b8dd-eecf7ea6b9da/image.png)

7097 개의 콘서트 결과를 조회하는데 115 ms 가 소요되었습니다.

**이로써 테스트 결과 인덱스를 적용한 조회 쿼리가 약 13% 정도의 성능 향상을 보여주었습니다.**

같은 방식으로 날짜와 좌석 조회 쿼리에도 인덱스를 적용하여 성능 테스트를 진행해 보겠습니다.

---

### 예약가능한 콘서트 날짜 조회

```sql
explain select
    coe1_0.id,
    coe1_0.available_seats,
    coe1_0.concert_id,
    c1_0.id,
    c1_0.concert_name,
    c1_0.end_date,
    c1_0.reserve_end_date,
    c1_0.reserve_start_date,
    c1_0.singer,
    c1_0.start_date,
    coe1_0.concert_date,
    coe1_0.concert_time,
    coe1_0.concert_venue,
    coe1_0.created_at,
    coe1_0.updated_by
from
    concert_option coe1_0
        join
    concert c1_0
    on c1_0.id=coe1_0.concert_id
where
    coe1_0.concert_id=?
```

![](https://velog.velcdn.com/images/asdcz11/post/58408fae-2b84-4ef4-a2c8-f2f8abbb7958/image.png)

실행결과 콘서트 Table 의 `PK` 는 인덱스가 적용되어 있지만, `concert_option Table` 의 `Join Column` 인 `concert_id` 에는 인덱스가 적용되어 있지 않습니다.

이 상태로 콘서트 날짜 조회 쿼리를 실행해보겠습니다. 

**테스트 코드**

```kotlin
@Test
fun `콘서트 날짜 조회 인덱스 테스트`() { 
    // 쿼리 성능 측정
    val duration = measureTimeMillis {
        val concerts = concertRepository.getAvailableDates(501L)
        println("콘서트 날짜 엔티티 개수 : ${concerts.size}")
    }

    // 결과 출력
    println("인덱스 적용 전 DB 조회 시간 : $duration ms")
}
```

#### 인덱스 추가 전 테스트 결과

![](https://velog.velcdn.com/images/asdcz11/post/c425eb06-c5dd-410d-b066-7d68433039b4/image.png)

#### 인덱스 추가 후 테스트 결과

![](https://velog.velcdn.com/images/asdcz11/post/f4e1537e-c247-4b73-9f2e-b7eb8009af53/image.png)


#### 조인 성능 특징
- **인덱스 사용**: 두 테이블 모두에서 인덱스가 성공적으로 사용되었습니다.
    - `콘서트 엔티티` (`concert` 테이블)은 기본 키(`PRIMARY`)를 사용하여 단일 행(`rows=1`)을 `const` 접근 방식으로 조회합니다.
    - `콘서트 옵션 엔티티` (`concert_option` 테이블)은 인덱스(`idx_concert_option_concert_id`)를 사용하여 `ref` 접근 방식으로 50개 행을 조회합니다.


![](https://velog.velcdn.com/images/asdcz11/post/94532246-8b5a-4a3e-81cd-4977f7b78848/image.png)

위 테스트 결과 조인하는 컬럼에 대해 인덱스를 추가하고 쿼리 실행시 **약 두 배 정도의 성능 향상을 보여주었습니다.**


### 예약가능한 콘서트 좌석 조회

예약가능 좌석 조회 쿼리는 다음과 같습니다.

```sql
explain select
    se1_0.id,
    se1_0.concert_option_id,
    se1_0.price,
    se1_0.seat_no,
    se1_0.seat_status
from
    seat se1_0
where
    se1_0.concert_option_id= :concert_option_id
  and se1_0.seat_status= :seat_status
```

테스트는 콘서트 날짜가 50개 있고, 각 날짜에 좌석 정보를 1000개씩 가지고 있는 환경에서 좌석 정보는 예약가능 한 좌석을 약 333~334 개 있도록 테스트를 진행했습니다.

#### 인덱스 추가 전 EXPLAIN 결과 요약
![](https://velog.velcdn.com/images/asdcz11/post/497d61ea-2797-4e32-8c9b-d89455243068/image.png)

| Field         | Value    |
|---------------|----------|
| id            | 1        |
| select_type   | SIMPLE   |
| table         | se1_0    |
| type          | ALL      |
| possible_keys | <null>   |
| key           | <null>   |
| key_len       | <null>   |
| ref           | <null>   |
| rows          | 97969    |
| Extra         | Using where |

- **전체 테이블 스캔**: `type` 필드가 `ALL`로 설정되어 있어, 이 쿼리는 전체 테이블 스캔을 수행하고 있습니다. 이는 인덱스가 적용되지 않았다고 볼 수 있습니다.
- **인덱스 미사용**: `key` 필드가 `null` 이므로, 어떠한 인덱스도 사용되지 않고 있습니다.

#### 인덱스 추가 전 테스트 결과

![](https://velog.velcdn.com/images/asdcz11/post/17d16511-5698-4636-9ad4-4eab5ae9ac9c/image.png)

**인덱스 추가 전 테스트 결과 168 ms 가 소요되었습니다.**

---

#### 인덱스 추가 후 EXPLAIN 결과

![](https://velog.velcdn.com/images/asdcz11/post/595a0b26-be83-4c07-8b06-c4b5c040f8a6/image.png)

| Field         | Value                              |
|---------------|------------------------------------|
| id            | 1                                  |
| select_type   | SIMPLE                             |
| table         | sel_0                              |
| type          | ref                                |
| possible_keys | idx_seat_status_concert_option_id  |
| key           | idx_seat_status_concert_option_id  |
| key_len       | 18                                 |
| ref           | const, const                       |
| rows          | 334                                |
| Extra         | Using index condition              |

- **인덱스 최적화 사용**: `type` 필드가 `ref`로 설정되어 있으며, `key`로 `idx_seat_status_concert_option_id` 인덱스가 사용되고 있습니다. 이는 쿼리가 해당 인덱스를 사용하여 관련 데이터를 효율적으로 찾고 있음을 의미합니다.
- **조건에 따른 인덱스 활용**: `ref`에 `const, const`로 표시되어 있어, 인덱스를 사용하는 데 있어 상수 값을 참조하고 있습니다.

---

위 두 쿼리와 다르게 where 문에 콘서트 옵션 테이블과의 조인을 위한 `concert_option_id` 와 `seat_status` 가 `AVAILABLE` 인지 확인하는 조건이 추가되었습니다.

지금까지는 단일 인덱스를 통해서 쿼리 성능을 올렸지만, 이렇게 where 절에 2개 이상의 조건이 추가되면 인덱스에 컬럼을 여러개 거는 복합 인덱스를 사용해서 쿼리 성능을 올릴 수 있습니다.

그러면 복합 인덱스를 어떻게 걸어야 성능적 이점을 얻을 수 있을지 알아보겠습니다.

복합 인덱스의 경우 인덱스의 순서를 어떻게 지정하는지에 따라서 쿼리 성능이 달라질 수 있습니다.

그렇다면 위 쿼리의 경우 `concert_option_id` 와 `seat_status` 중 어떤 컬럼이 먼저 와야할까요??

`concert_option_id` 가 먼저 오는 경우와 `seat_status` 가 먼저 오는 경우 두 가지 경우를 비교해보겠습니다.

#### `concert_option_id` 가 먼저 오는 경우

![](https://velog.velcdn.com/images/asdcz11/post/aa9a8ff5-4731-4e54-9f85-28916adaa3ee/image.png)

**약 10번의 테스트 결과 평균 130 ~ 140 ms 가 소요 되었습니다.**

#### `seat_status` 가 먼저 오는 경우

![](https://velog.velcdn.com/images/asdcz11/post/127e08a9-e60f-446e-b5d0-27457fc5d7f5/image.png)

**약 10번의 테스트 결과 평균 120 ~ 130 ms 가 소요 되었습니다.**

---

#### 결론

`seat_status`가 먼저 오는 경우가 `concert_option_id`가 먼저 오는 경우보다 약 10 ms 정도 더 빠른 속도를 보여주었습니다.

이유는 인덱스의 순서를 지정할 때는 카디널리티(데이터 중복도가 낮은)가 매우 중요한데, `seat_status`의 경우 3개 종류밖에 없지만 `concert_option_id`의 경우 `PK`이기 때문에 카디널리티가 높습니다.

카디널리티가 높은 컬럼이 먼저 오는 경우 뒤에 오는 컬럼의 카디널리티가 낮으면 복합 인덱스를 추가해도 의미 없는 인덱스가 될 확률이 매우 높습니다.

예를 들면, `seat_status`가 `AVAILABLE`인 좌석을 먼저 인덱스를 통해 빠르게 탐색한 뒤 `concert_option_id`가 40인 좌석을 조회하는 복합 인덱스가
`concert_option_id`가 40인 좌석 중 `seat_status`가 `AVAILABLE`인 좌석을 찾는 복합 인덱스보다 더 효율적으로 조회 성능을 끌어올릴 수 있습니다.

따라서 **두 개 이상의 컬럼을 조합하여 복합 인덱스를 사용한 쿼리의 결과가 사용하지 않은 쿼리보다 약 35% 정도 빠른 속도를 보여주었습니다.**

---

## 인덱스는 정말 항상 적용될까?

지금까지 인덱스를 사용하기 전, 후의 데이터 조회 성능을 비교해보았습니다.

하지만 테스트중 인덱스를 추가했지만 모든 조회에서 인덱스가 적용되지는 않았습니다.
예를들면, 콘서트 조회에서 `2024-08-03` 으로 조회하면 인덱스를 통한 조회가 아닌 테이블 풀스캔으로 데이터를 조회하는 경우가 있었습니다.

왜 그런걸까요??

우선 제가 테스트한 환경은 MariaDB 입니다.

MariaDB 에서 쿼리 실행 계획은 `Optimizer` 에 의해 결정됩니다. `Optimizer` 는 사용자로부터 입력된 SQL 쿼리를 가장 효율적인 방식으로
실행하기 위한 최적의 실행 경로를 결정합니다.

MariaDB 의 `Optimizer` 는 `cost-based` 옵티마이저로, 쿼리 실행 계획을 결정할 때 쿼리의 비용을 계산하여 가장 효율적인 실행 계획을 선택합니다.
이 예상 비용을 계산하는 과정에서 통계정보 (테이블 크기, 인덱스, 데이터 분포 등) 를 활용합니다.

따라서 모든 검색 조건에 대해 인덱스가 항상 사용되지는 않습니다. Optimizer는 다음과 같은 요인들을 고려하여 인덱스 사용 여부를 결정합니다.

1. `데이터의 분포`: 특정 날짜 (2024-08-03) 에 해당하는 데이터가 전체 데이터의 상당 부분을 차지한다면, Optimizer는 인덱스를 사용하는 것보다 테이블 풀스캔이 더 효율적이라고 판단할 수 있습니다.


2. `테이블의 크기`: 테이블이 작다면, 인덱스를 사용하는 것보다 전체 테이블을 스캔하는 것이 더 빠를 수 있습니다.


3. `인덱스의 선택도(Selectivity)`: 인덱스가 데이터를 얼마나 잘 분류하는지에 따라 사용 여부가 결정됩니다. 선택도가 낮으면 (즉, 많은 레코드가 동일한 인덱스 값을 가지면) 인덱스의 효용이 떨어집니다.


4. `쿼리의 복잡성`: 복잡한 조인이나 서브쿼리가 포함된 경우, Optimizer의 결정이 달라질 수 있습니다.


위와 같은 이유로 `2024-08-03` 과 같은 특정 날짜로 조회할 때 인덱스를 사용하지 않고 풀스캔을 하는 것은, Optimizer가 이런 요인들을 고려하여 풀스캔이 더 효율적이라고 판단했기 때문입니다. 때로는 실제로 풀스캔이 더 빠를 수 있습니다.

그러나 만약 이러한 Optimizer의 판단이 실제 성능과 맞지 않는다고 생각된다면, 쿼리 힌트를 사용하여 인덱스 사용을 강제하거나, 인덱스를 재구성하거나, 또는 통계 정보를 업데이트하는 등의 방법을 시도해 볼 수 있습니다.

(MariaDB 에서는 `USE INDEX` 또는 `FORCE INDEX` 힌트를 사용하여 특정 인덱스를 사용하도록 강제할 수 있을 것 같습니다)

---

## 정리하며...

이번 인덱스 문서 정리를 통해 인덱스를 정하기 전과 후 의 쿼리 성능 차이를 알아보았습니다.

복합 인덱스를 사용할때는 인덱스의 순서를 생각하면 어떤 컬럼이 먼저 오는게 좋을지 고민해보는 것이 중요하다는 것을 알 수 있었습니다.

인덱스를 사용할때 인덱스를 읽고, 인덱스를 바탕으로 데이터를 찾아가는 과정보다 풀스캔을 통한 데이터 검색이 더 빠른 경우가 있기 때문에 무조건 인덱스를 통한 검색이 더 빠르다라고 할 수 없다는 내용도 알아볼 수 있었습니다.

이로써 쿼리의 특성을 잘 파악하고 적절한 인덱스를 사용하는 것이 중요하다는 것을 알 수 있었습니다.