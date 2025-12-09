# ElasticSearch 도입

콘서트 조회 API는 현재 MySQL의 LIKE 문을 사용하고 있습니다.

```sql
SELECT * FROM concert 
WHERE concert_name LIKE '%검색어%' OR singer LIKE '%검색어%'
```

이때 요구사항 변경으로 아래와 같이 요구사항이 변경되었을 경우 기존의 Mysql 의 LIKE 문으로 대응하기 어려워졌습니다.

| 요구사항 | LIKE 문 한계 | ElasticSearch 해결 |
|---------|-------------|-------------------|
| **자동완성** | 불가능 | Completion Suggester 지원 |
| **오타 허용** | "아이유" 검색 시 "아유이" 매칭 불가 | Fuzzy Search로 오타 2자까지 허용 |
| **동의어 검색** | "BTS" 검색 시 "방탄소년단" 매칭 불가 | Synonym Filter로 동의어 처리 |
| **형태소 분석** | "아이유콘서트" → 단순 문자열 | "아이유", "콘서트" 토큰으로 분리 |
| **성능** | 전체 테이블 스캔 (느림) | 역색인으로 빠른 조회 |


**검색 품질 개선 예시:**
- 사용자가 "아유이"로 검색해도 "아이유 콘서트"를 찾을 수 있음
- "BTS" 검색 시 "방탄소년단" 콘서트도 함께 노출
- "아이" 입력 시 "아이유", "아이브" 등 자동완성 제안

프로젝트는 각 레이어를 인터페이스로 추상화하여 구현했기 때문에, `ElasticSearch` 구현체만 추가하면 기존 로직 수정 없이 검색 엔진을 교체할 수 있습니다.

```kotlin
// 추상화된 인터페이스
interface ConcertReadRepository {
    fun getConcerts(...): List
}

// ElasticSearch 구현체만 추가
class ConcertElasticSearchRepository : ConcertReadRepository {
    override fun getConcerts(...): List {
        // ES 구현
    }
}
```

---

## ElasticSearch 데이터 이동

콘서트 생성 시 MySQL과 ElasticSearch 모두에 데이터를 저장해야 합니다.
이벤트 기반 비동기 처리로 API 응답 속도 저하를 방지했습니다.

혹시라도 ElasticSearch 저장에 실패하더라도 MySQL에는 정상 저장되기 때문에 데이터 유실이 발생하지 않습니다.

때문에 DB 와 ElasticSearch 간의 정합성을 맞추는 배치로직이 있다면 비동기통신으로 인한 데이터 정합성 불일치 문제도 해결가능합니다.

### 데이터 흐름
```
[콘서트 생성 요청]
      ↓
[ConcertService]
      ↓
[MySQL 저장] ──→ [이벤트 발행: ConcertCreatedEvent]
      ↓                          ↓
[응답 반환]                  [EventListener]
                                 ↓
                           [ElasticSearch 저장]
```

---

## ElasticSearch 검색

`ElasticSearch`는 외부 시스템이고 HTTP 통신을 사용하므로 장애 가능성을 고려해야 합니다.
`ElasticSearch` 검색 실패 시 자동으로 MySQL로 Fallback하여 **서비스 중단을 방지**합니다.

```kotlin
override fun getConcerts(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertEntity> {
    try {
        val documents = concertSearchRepository.searchByOptions(concertName, singer, startDate?.toString(), endDate?.toString())
        return documents
            .map { d ->
                ConcertEntity(
                    id = d.id.toLong(),
                    concertName = d.concertName,
                    singer = d.singer,
                    startDate = d.startDate,
                    endDate = d.endDate,
                    reserveStartDate = d.reserveStartDate,
                    reserveEndDate = d.reserveEndDate,
                )
            }
    } catch (_: Exception) {
        return searchWithJpa(concertName, singer, startDate, endDate)
    }
}
```

위와 같이 ElasticSearch 도입을 통해서 아이유 콘서트를 조회하려다 오타로 아유이 콘서트로 조회를 해도 조회가 가능하며,
콘서트 아이유 로 순서를 변경해도 조회가 가능해졌습니다.

---

## 자동완성 API

ElasticSearch 에서는 자동완성을 하고싶은 필드(컬럼)를 `FieldType.Search_As_You_Type` 으로 지정하면 손쉽게 자동완성 API 구현이 가능합니다.
콘서트 이름 검색에 자동완성 설정을 추가하였고 NativeQuery 는 아래와 같이 작성했습니다.

**검색 쿼리:**
```kotlin
override fun getSuggestions(keyword: String): List {
    val query = MultiMatchQuery.of { m ->
        m.query(keyword)
            .fields(
                "concertName.auto_complete",         // 기본 필드
                "concertName.auto_complete._2gram",  // 2글자 단위
                "concertName.auto_complete._3gram"   // 3글자 단위
            )
            .type(TextQueryType.BoolPrefix)
    }._toQuery()

    val nativeQuery = NativeQuery.builder()
        .withQuery(query)
        .withMaxResults(10)  // 상위 10개만 반환
        .build()

    return elasticSearchOperations
        .search(nativeQuery, ConcertDocument::class.java)
        .map { it.content.concertName }
        .distinct()  // 중복 제거
        .toList()
}
```

**동작 예시:**
```
사용자 입력: "아이"
  ↓
자동완성 결과:
  - 아이유 콘서트
  - 아이유 앵콜 콘서트
  - 아이브 단독 콘서트
```

### 트래픽 관리: Rate Limiting

자동완성 API는 **사용자가 한 글자 입력할 때마다 호출**되므로 트래픽 급증 위험이 있습니다.
이를 방지하기 위해 `resilience4j`의 Rate Limiter를 적용했습니다.

위 문제를 방지하기 위해서 `resilience4j` 의 `ratelimiter` 를 사용하여 여러번 api 가 호출되지 않도록하여 해결했습니다.

```yaml
resilience4j:
  ratelimiter:
    instances:
      autocomplete:
        limit-for-period: 30 # 허용횟수
        limit-refresh-period: 1s # 리프레시주기
        timeout-duration: 0s
```
위설정을 통해서 1초에 자동완성 api 의 경우 30번 호출이 가능하고 1초가 지나면 다시 0으로 리셋됩니다.

만약 1초에 3번 이상의 api 호출이 진행된경우 `TOO_MANY_REQUESTS` 에러가 발생합니다.

```kotlin
@RateLimiter(name = "autocomplete", fallbackMethod = "throwRateLimitEx")
override fun getConcertSuggestions(query: String): List<String> {
    return concertSearchRepository.getSuggestions(query)
}

private fun throwRateLimitEx(query: String, ex: Throwable): List<String> {
    throw ConcertException(ErrorCode.RATE_LIMIT_EXCEEDED)
}
```


**동작 방식:**
- 1초에 최대 30회 호출 허용
- 1초 경과 시 카운터 리셋
- 초과 호출 시 `429 TOO_MANY_REQUESTS` 에러 반환


## 결론

ElasticSearch 를 통해서 콘서트 조회 기능에대한 사용자 만족도를 높일수 있었습니다.ㄴ

