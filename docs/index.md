# DB의 인덱스란??
데이터베이스의 인덱스는 테이블 내의 데이터를 빠르게 검색할 수 있도록 도와주는 자료구조로 익히 알려져 있습니다.
인덱스를 사용하면 데이터베이스는 전체 테이블을 스캔하지 않고도 원하는 데이터를 효율적으로 찾아낼 수 있습니다. 
하지만 인덱스도 장점만은 있지 않겠죠 ? 인덱스를 사용하면 데이터의 삽입, 수정, 삭제가 느려지는 단점이 있습니다.

이제 부터 콘서트 좌석 예약 시스템에서 인덱스를 적용하여야할 쿼리는 어떤 부분이며 어떻게 적용해야할지 알아보겠습니다.

## 인덱스를 적용해야할 쿼리

콘서트 좌석 예약 시스템에서 조회쿼리를 사용하는 부분은 다음과 같습니다.

1. 예약가능한 콘서트 목록 조회
2. 예약가능한 콘서트 날짜 조회
3. 예약가능한 콘서트 좌석 조회

## JPA 인덱스 적용 방법


## 10만개의 데이터로 쿼리속도 비교 (인덱스 미적용 / 적용)

약 10만개의 데이터를 미리 생성 후 인덱스를 사용한 쿼리와 사용하지 않은 쿼리의 속도를 비교해보겠습니다.

EXPLAIN을 사용하면 WHERE 절을 사용해서 검색하는지 아니면 인덱스를 사용하는지 확인할 수 있습니다.

### 예약가능한 콘서트 목록 조회

인덱스는 start_date에 적용하여 쿼리를 실행해보겠습니다.

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

**인덱스 적용전 Explain 쿼리 결과와 쿼리 시간 계산 결과**
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

**인덱스 미사용**: `key` 필드가 <null>이므로, 어떠한 인덱스도 사용되지 않고 있습니다.

![](https://velog.velcdn.com/images/asdcz11/post/0478f21c-6a8c-4e83-a5fb-659eeaf71169/image.png)

7097 개의 콘서트 결과를 조회하는데 132 ms 가 소요되었습니다.

이제 같은 데이터에 인덱스를 적용한 후 쿼리를 실행해보겠습니다.

**인덱스 적용후 Explain 쿼리 결과와 쿼리 시간 계산 결과**

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
- **효율적인 행 필터링**: `rows` 값이 14192로 나타나, 이 인덱스가 쿼리 성능을 향상시키는 데 기여하고 있음을 보여줍니다. 상대적으로 적은 수의 행을 스캔함으로써 성능을 최적화하고 있습니다.

![](https://velog.velcdn.com/images/asdcz11/post/8c016853-3fe7-45f1-b8dd-eecf7ea6b9da/image.png)

7097 개의 콘서트 결과를 조회하는데 115 ms 가 소요되었습니다.

**이로써 테스트 결과 인덱스를 적용한 조회 쿼리가 약 13% 정도의 성능 향상을 보여주었습니다.**

같은 방식으로 날짜와 좌석 조회 쿼리에도 인덱스를 적용하여 성능 테스트를 진행해 보겠습니다.

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

![](https://velog.velcdn.com/images/asdcz11/post/1809edac-ab33-4ceb-a8a5-831f256ca411/image.png)

실행결과 콘서트 Table 의 PK는 인덱스가 적용되어 있지만, concert_option Table 의 Join Column 인 concert_id 에는 인덱스가 적용되어 있지 않습니다.

이 상태로 콘서트 날짜 조회 쿼리를 실행해보겠습니다. 

**테스트 코드**

```kotlin

```

**인덱스 추가 전 테스트 결과**

![](https://velog.velcdn.com/images/asdcz11/post/c425eb06-c5dd-410d-b066-7d68433039b4/image.png)

**인덱스 추가 후 테스트 결과**

![](https://velog.velcdn.com/images/asdcz11/post/94532246-8b5a-4a3e-81cd-4977f7b78848/image.png)

이로써 조인하는 컬럼에 대해 인덱스를 걸고 쿼리 실행시 약 두 배 정도의 성능 향상을 보여주었습니다.


### 예약가능한 콘서트 좌석 조회

```sql

```

## 정리하며...