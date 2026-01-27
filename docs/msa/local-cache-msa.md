# 분산환경에서의 로컬캐시 적용 해보기

## 도입 배경

MSA 같은 분산환경 혹은 대용량 처리 혹은 서버의 고가용성을 위한 Scale Out 상황에서 캐싱을 위한 도구로 Redis를 주로 사용합니다.

콘서트 예매 시스템에서는 콘서트 정보(공연장, 일정 등)에 대한 조회가 매우 빈번하게 발생합니다. 이러한 변경 빈도가 낮은 메타 데이터를 매번 DB에서 조회하는 것은 비효율적입니다.

로컬캐시만 사용하면 분산환경에서 데이터 일관성을 맞추기 어렵기 때문에 서비스 외부에 캐시 저장소를 두어서 여러 서비스에 공유하는 방식을 사용합니다.

실제 프로젝트에서도 `Local Cache` -> `Redis Cache`로 전환도 직접 적용해 보았었습니다. [캐시 적용 문서](/docs/caching.md)

하지만 `Redis`를 사용하는 것이 꼭 장점만 있는 것은 아니었습니다.

### Redis 캐시의 단점

- 네트워크 비용 발생 : 캐시 조회시 네트워크 왕복 비용이 발생합니다. (로컬캐시는 메모리 접근이므로 매우 빠릅니다)
- 운영 비용 발생 : Redis 서버를 별도로 운영해야 하므로 인프라 비용과 운영 비용이 발생합니다.
- 장애 지점 추가 : Redis 서버가 다운되면 캐시를 사용할 수 없게 됩니다. (로컬캐시는 서비스 인스턴스가 살아있다면 캐시 사용 가능)

위 단점들을 해결하기 위해서 로컬캐시를 분산환경에서도 사용할 수 있도록 하고, Redis의 경우 보조적으로 꼭 사용해야 하는 경우에만 사용할 수 있도록 하는 방법을 고민해 보았습니다.

### CacheManager를 활용한 통합 캐시 처리

우선 현재 캐시 작업 자체가 Redis에 의존적으로 코드가 작성되어 있기에 이를 `LocalCache`, `RedisCache` 둘 다 상황에 맞춰 사용할 수 있도록 `CacheManager`를 구현했습니다.


```kotlin
sealed class MyCache(
    val type: CacheType,
) {
    val name: String = this.javaClass.simpleName
}

sealed class LocalCache: MyCache(CacheType.LOCAL) {
    data object MetaCache : LocalCache()
}

sealed class RedisCache : MyCache(CacheType.REDIS) {
    data object MetaCache : RedisCache()
}
```

`sealed class` 를 사용해서 해당 파일에서만 제한된 계층구조를 가지도록 해서 `Local`, `Redis` 로 구분했습니다.

```kotlin
@Component
class CacheManager(
    private val localCacheManager: SimpleCacheManager,
    private val redisCacheManager: RedisCacheManager,
) {

    private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP))

    /**
     * clazz : key 로 조회 후 clazz 타입으로 반환한다
     * block : key 로 조회한 값이 없을 경우 실행할 블록 -> 이후 조회된 값을 캐시로 사용
     */
    fun <T> getOrPut(
        cache: MyCache,
        key: String,
        clazz: Class<T>,
        block: () -> T?,
    ): T? = runCatching {
        get(cache, key, clazz)
    }.getOrElse {
        block()?.also {
            put(cache, key, it)
        }
    }

    fun <T> get(cache: MyCache, key: String, clazz: Class<T>): T? =
        runCatching {
            val wrapper = getByCache(cache).get(key)
                ?: throw ConcertException(ErrorCode.FAILED_TO_HANDLE_CACHE, "캐시가 존재하지 않습니다")

            val rawValue = wrapper.get()
                ?: throw ConcertException(ErrorCode.FAILED_TO_HANDLE_CACHE, "캐시 데이터가 null 입니다")

            when (cache.type) {
                CacheType.LOCAL -> {
                    if (clazz.isInstance(rawValue)) {
                        rawValue as T
                    } else {
                        throw IllegalArgumentException("로컬 캐시의 타입과 맞지 않습니다")
                    }
                }
                CacheType.REDIS -> {
                    when {
                        clazz.isInstance(rawValue) -> rawValue as T
                        rawValue is Map<*, *> -> objectMapper.convertValue(rawValue, clazz)
                        rawValue is LinkedHashMap<*, *> -> objectMapper.convertValue(rawValue, clazz)
                        else -> throw IllegalArgumentException("변환 불가 ${rawValue::class.java} : $clazz")
                    }
                }
            }
        }.recoverCatching { exception ->
            log.error("캐시 작업 실패", exception)
            evict(cache, key)
            throw ConcertException(ErrorCode.FAILED_TO_HANDLE_CACHE, exception.message, exception)
        }.getOrThrow()

    fun put(cache: MyCache, key: String, value: Any) =
        runCatching {
            getByCache(cache).put(key, value)
        }.recoverCatching {exception ->
            throw ConcertException(ErrorCode.FAILED_TO_HANDLE_CACHE, null, exception)
        }.getOrThrow()

    fun evict(cache: MyCache, key: String) =
        runCatching {
            getByCache(cache).evict(key)
        }.getOrDefault(Unit)

    private fun getByCache(cache: MyCache) =
        when (cache.type) {
            CacheType.LOCAL -> localCacheManager.getCache(cache.name)
            CacheType.REDIS -> redisCacheManager.getCache(cache.name)
        } ?: throw ConcertException(ErrorCode.FAILED_TO_HANDLE_CACHE)


    fun <T> handleCacheByAction(actionType: ActionType, cache: MyCache, key: String, block: () -> T?) {
        when (actionType) {
            ActionType.CREATE, ActionType.UPDATE -> {
                block()?.also {
                    put(cache, key, it)
                }
            }
            ActionType.DELETE -> {
                evict(cache, key)
            }
        }
    }
}
```

위와 같이 `CacheManager`를 구현해서 `MyCache` 타입에 따라 `LocalCache`, `RedisCache`를 구분해서 사용할 수 있도록 했습니다.

이로써 사용하는 코드에서 `CacheType`이 `Local`인지 `Redis`인지만 결정해주면 상황에 따라 유연하게 캐싱 전략을 사용할 수 있습니다.

### 로컬 캐시의 데이터 정합성 문제 해결하기

위 CacheManager를 통해서 로컬캐시도 적용할 수 있도록 했습니다.

하지만, 로컬캐시의 가장 큰 문제인 분산환경에서의 데이터 정합성 문제가 남아있었습니다. 

콘서트 예매 시스템의 메타 데이터는 다음과 같은 특성을 가지고 있습니다:

- **변경 빈도가 낮음**: 콘서트 정보, 공연장 정보 등은 자주 변경되지 않습니다
- **실시간 동기화 불필요**: 콘서트 일정이나 좌석 정보가 변경되더라도, 수 초~수 분 내에 모든 서버에 반영되면 충분합니다
- **TTL 기반 자동 갱신**: 1시간 TTL을 설정하여, 최악의 경우에도 1시간 내에는 모든 서버의 데이터가 일치하게 됩니다

이러한 이유로 **최종적 일관성(Eventual Consistency)** 모델을 채택했습니다. 즉, 일시적인 데이터 불일치는 허용하되, 최종적으로는 모든 서버의 데이터가 일치하도록 보장하는 방식입니다.

데이터 정합성을 맞추기 위해서는 변경된 내용을 다른 인스턴스로 알려줘서 데이터 최신화를 하는 작업이 필요했습니다. 이미 유저 회원가입 기능에 Kafka를 통한 이벤트 기반 아키텍처가 적용되어 있었기 때문에 이를 활용하기로 했습니다.

현재 프로젝트에서 이미 Kafka를 사용하고 있기 때문에 Kafka를 통해서 이벤트를 발송하고, 발행된 이벤트를 컨슘하여 캐시를 최신화하도록 개발했습니다.


### 실제 멀티 인스턴스 환경에서 로컬캐시의 문제점 확인

테스트 환경은 로컬 스프링 서버 2대를 띄워둔 상황에서 진행했습니다

1. 서버 A, B 모두 Local Cache만 사용하도록 설정 -> DB에 저장되어 있는 값을 동일하게 캐시에 로드

![](https://velog.velcdn.com/images/asdcz11/post/9de29796-9b81-49a2-b4db-d2fd5f79283a/image.png)

2. 서버 A에서 캐시 변경 -> 장소, 인원수가 서버 A에서만 변경

![](https://velog.velcdn.com/images/asdcz11/post/3cd6d38e-2a34-4db5-89a5-ec38cb59df00/image.png)

3. 서버 B에서 캐시 조회 -> 서버 A에서 변경된 캐시가 반영되지 않음 (데이터 불일치) -> 서버 A에서 재조회 시 변경된 내용이지만 서버 B에서는 여전히 변경 전 데이터

### 캐시 변경 이벤트 발행 및 구독 추가

이제 콘서트 옵션에 대한 데이터 변경 시 내부 이벤트 발행 후 Kafka로 발행하도록 코드를 추가했습니다.

**메시지 유실 가능성과 대응 방안:**
- Kafka를 사용함에 따라 메시지 유실 가능성도 존재하긴 합니다
- 하지만 캐시를 사용하는 지점은 변경 가능성이 적은 데이터이기 때문에 메시지 발생량이 적습니다
- 캐시 TTL 자체가 1시간이므로, 메시지가 유실되더라도 1시간 후에는 데이터 정합성이 맞춰집니다
- 콘서트 정보 같은 민감한 데이터도 정해진 주기에 따라 변경되거나 노출이 종료되기 때문에 일시적 불일치가 서비스에 치명적인 영향을 주지 않습니다

```kotlin
override fun changeConcertOption(concertId: Long, concertOptionId: Long, command: ChangeConcertOptionCommand) {
    val concertOption = searchConcertOption(concertOptionId)
    concertOption.update(
        availableSeats = command.availableSeats,
        concertDate = command.concertDate,
        concertTime = command.concertTime,
        concertVenue = command.concertVenue,
    )
    val concertOptionChangeEvent = createConcertOptionEvent(concertId, concertOption)
    refreshCache(concertId)

    eventPublisher.publish(concertOptionChangeEvent) // -> 이벤트 발행
}
```
컨슈머에서는 ACTION 에 따라서 캐시에 대한 evict, update 작업을 수행하도록 했습니다.

```kotlin
fun concertOptionCacheRefreshConsumer(eventString: String ) {
    try {
        val event = JsonUtil.decodeFromJson<ConcertOptionChangeEvent>(eventString)

        val concertId = event.concertId
        val key = "${ConcertDatesCache::class.java.simpleName}:${concertId}"

        cacheManager.handleCacheByAction(ActionType.UPDATE, LocalCache.MetaCache, key) {
            ConcertDatesCache.from(concertReadRepository.getAvailableDates(concertId))
        }
    } catch (e: ConcertException) {
        log.error("ConcertException 발생", e)
        throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
    } catch (e: Exception) {
        log.error("이벤트 처리 실패 : {}", eventString, e)
        throw e
    }
}
```


### 결과

위와 같이 캐시 변경 이벤트 발행 및 구독을 추가한 이후에는 멀티 인스턴스 환경에서도 로컬캐시를 사용하면서도 데이터 정합성을 맞출 수 있었고, 이에 따라 Redis 장애 시에도 로컬 캐시를 통해 서비스 지속 가능하게 되었습니다.

### 마치며

로컬캐시를 분산환경에서 사용하는 것은 데이터 정합성 문제로 인해 어려움이 있었지만, 최종적 일관성을 적용하여 캐시 변경 이벤트를 발행하고 구독 방식을 통해 이를 해결해 보았습니다.

좀 더 부수적으로 나아간다면 Redis 를 보조적으로 사용해서 로컬캐시에 없는 데이터는 Redis 에서 조회하고 Redis 에도 없는 경우에만 DB 조회하는 방식으로도 확장할 수 있을 것 같습니다.


### 참고 자료

[분산 시스템에서 로컬 캐시 활용하기](https://tech.kakaopay.com/post/local-caching-in-distributed-systems/)

