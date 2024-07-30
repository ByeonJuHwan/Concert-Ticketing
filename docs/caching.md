# 캐싱이란?

DB의 Connection과 I/O는 매우 높은 비용을 요구하며, 트래픽이 많아질수록 이러한 비용은 더욱 증가합니다. 캐싱(caching)은 자주 사용되거나 접근 시간이 중요한 데이터를 임시 저장소에 보관하여, 데이터베이스 또는 원본 소스에 대한 직접 접근을 줄이고 응답 시간을 단축시키는 기술입니다. 
이를 통해 시스템의 성능을 향상시킬 수 있으며, 특히 대규모 트래픽 상황에서 캐싱 전략을 적절히 활용하면 시스템의 부하를 효율적으로 관리할 수 있습니다.

일반적으로 캐싱을 적용하면 데이터를 빠르게 가져올 수 있다는 장점이 있지만, 데이터의 일관성을 유지하는 것이 어려워질 수 있습니다. 
이는 캐시에 저장된 데이터가 변경되었을 때, 캐시와 데이터베이스의 데이터가 일치하지 않게 되기 때문입니다.

그렇다면 콘서트 좌석 예약 프로젝트는 어떤 데이터를 캐싱해야 할까요??

제가 생각하는 캐싱 대상은 다음과 같습니다.

1. 콘서트 목록
2. 콘서트 상세 정보

**왜냐하면 콘서트 목록과 콘서트 상세 정보는 자주 조회되는 데이터이며 한번 콘서트 정보에 대해 저장되면 변경이 될 일이 드물다고 생각합니다.**

---

# 로컬 캐싱

캐싱저장소는 주로 Redis 를 사용하지만, 우선 먼저 Redis 를 사용해서 캐싱을 적용해 보기전에 로컬 캐싱을 사용해서 캐싱을 해보고 이후에 왜 Redis 를 사용 하는지 알아보겠습니다.

로컬 캐싱은 메모리에 데이터를 저장하는 방식으로, 스프링에서는 `@Cacheable` 어노테이션을 사용하여 메소드의 결과를 캐싱할 수 있습니다.


1. `@EnableCaching` 어노테이션 추가
   ```kotlin
    @EnableCaching
    @SpringBootApplication
    class ConsertApplication
    fun main(args: Array<String>) {
        runApplication<ConsertApplication>(*args)
    }  
   ```
2. 캐싱할 메소드에 `@Cacheable` 어노테이션 추가
   ```kotlin
   /**
    * 콘서트 목록을 가져오는 메소드에 캐싱을 적용합니다.
    * 캐싱할 메소드에 @Cacheable 어노테이션을 추가합니다.
    * value 속성은 캐시의 이름을 지정합니다.
    */ 
   @Cacheable("concerts")
    fun getConcerts(): List<Concert> {
        return concertRepository.findAll()
    }
   ```
   
콘서트 목록 100 개를 저장하고 100개의 데이터를 단순 조회하는데 걸리는 시간을 측정해보겠습니다.


```kotlin
@Test
fun `캐싱을 활용한 콘서트 목록 조회 테스트`() {
   // 첫 번째 호출: DB 에서 데이터를 조회해 가져옵니다
   val startTime1 = System.currentTimeMillis()
   val concerts1 = concertService.getConcerts()
   val endTime1 = System.currentTimeMillis()
   val duration1 = endTime1 - startTime1

   assertEquals(100, concerts1.size)
   println("DB 에서 콘서트 목록 조회 소요 시간 : $duration1 ms")

   // 두 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
   val startTime2 = System.currentTimeMillis()
   val concerts2 = concertService.getConcerts()
   val endTime2 = System.currentTimeMillis()
   val duration2 = endTime2 - startTime2

   assertEquals(100, concerts2.size, "The number of concerts should be 50")
   println("로컬 캐싱에서 콘서트 목록 조회 소요 시간 : $duration2 ms")

   assertSame(concerts1, concerts2)
   assertTrue(duration2 < duration1)
}
```

**실행 결과**

![](https://velog.velcdn.com/images/asdcz11/post/d9c1798f-973f-4d72-bfe0-d1aa065ab9f2/image.png)

실행결과로 보아 DB 에서 100 개의 콘서트 목록을 조회할때는 9ms 가 걸린 반면 로컬 캐싱을 사용할때는 0ms 로 거의 즉시 조회가 되어 속도가 엄청 빠른걸 확인 가능합니다


다음은 `Fetch join` 을 사용하는 콘서트 예약가능 날짜 정보 조회 메소드에 캐싱을 적용해보겠습니다.

```kotlin
@Test
fun `캐싱을 활용한 콘서트 예약가능 날짜 목록 조회 테스트`() { 
    // 첫 번째 호출: DB 에서 데이터를 조회해 가져옵니다 
    val startTime1 = System.currentTimeMillis()
    val concertOptions1 = concertService.getAvailableDates(1L)
    val endTime1 = System.currentTimeMillis()
    val duration1 = endTime1 - startTime1

    assertEquals(100, concertOptions1.size)
    println("DB 에서 콘서트 예약가능 날짜 목록 조회 소요 시간 : $duration1 ms")

    // 두 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
    val startTime2 = System.currentTimeMillis()
    val concertOptions2 = concertService.getAvailableDates(1L)
    val endTime2 = System.currentTimeMillis()
    val duration2 = endTime2 - startTime2

    assertEquals(100, concertOptions2.size)
    println("로컬 캐싱에서 콘서트 예약가능 날짜 목록 조회 소요 시간 : $duration2 ms")

    assertSame(concertOptions1, concertOptions2)
    assertTrue(duration2 < duration1)
}
```

**실행 결과**

![](https://velog.velcdn.com/images/asdcz11/post/c48fea68-25bd-4dc4-a32c-81a48c9e242f/image.png)

`Fetch join` 이 있는 쿼리라도 실제 DB 에서 조회하는 10ms 가 걸리는 반면 로컬 캐싱을 사용할때는 1ms 로 역시나 속도가 매우 빠릅니다.

이렇게 실제 캐싱을 적용하는데는 매우 간단하게 적용할 수 있습니다. 하지만 캐싱을 사용할때는 캐시의 일관성을 유지하는 것이 중요합니다. 
기존 100개의 데이터에서 1개의 콘서트 데이터가 추가되었을때 캐시된 데이터는 아직 100개의 데이터이므로 캐시의 일관성이 깨집니다.

따라서 저는 테스트로 스케줄러가 3초 뒤에 콘서트 목록 조회 캐시 데이터를 삭제 시키고 DB 에서 콘서트 목록을 조회하게 하여 캐시의 일관성을 유지하도록 테스트를 해보겠습니다.
```kotlin
    /**
    * 3초 마다 concerts 캐시를 삭제합니다.
    */
    @CacheEvict(value = ["concerts"], allEntries = true)
    @Scheduled(fixedRate = 3000)
    fun clearConcertsCache() {
    }
```
```kotlin
@Test
fun `캐싱을 활용한 콘서트 목록 조회 후 캐시 삭제 테스트`() {
   // 첫 번째 호출: DB 에서 데이터를 조회해 가져옵니다
   val startTime1 = System.currentTimeMillis()
   val concerts1 = concertService.getConcerts()
   val endTime1 = System.currentTimeMillis()
   val duration1 = endTime1 - startTime1

   assertEquals(100, concerts1.size)
   println("DB 에서 콘서트 목록 조회 소요 시간 : $duration1 ms")

   // 콘서트 데이터 1개 추가
   insertNewConcert()

   // 두 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
   val startTime2 = System.currentTimeMillis()
   val concerts2 = concertService.getConcerts()
   val endTime2 = System.currentTimeMillis()
   val duration2 = endTime2 - startTime2

   assertEquals(100, concerts2.size)
   println("로컬 캐싱에서 콘서트 목록 조회 소요 시간 : $duration2 ms")

   assertSame(concerts1, concerts2)
   assertTrue(duration2 < duration1)

   // 3초 대기: 캐시가 만료되도록 대기합니다.
   Thread.sleep(3000)

   // 세 번째 호출: 캐시가 만료되어 다시 DB 에서 데이터를 조회해 가져옵니다
   val startTime3 = System.currentTimeMillis()
   val concerts3 = concertService.getConcerts()
   val endTime3 = System.currentTimeMillis()
   val duration3 = endTime3 - startTime3

   assertEquals(101, concerts3.size)
   println("캐시 만료 후 DB 에서 콘서트 목록 조회 소요 시간 : $duration3 ms")

   assertNotSame(concerts2, concerts3)
   assertTrue(duration3 > duration2)

   // 네 번째 호출: 캐시가 되어 있으므로 캐시에서 데이터를 가져옵니다.
   val startTime4 = System.currentTimeMillis()
   val concerts4 = concertService.getConcerts()
   val endTime4 = System.currentTimeMillis()
   val duration4 = endTime4 - startTime4

   assertEquals(101, concerts4.size)
   println("로컬 캐싱에서 콘서트 목록 조회 소요 시간 : $duration4 ms")

   assertSame(concerts3, concerts4)
   assertTrue(duration4 < duration3)
}
```

**실행 결과**

![](https://velog.velcdn.com/images/asdcz11/post/8f3030f9-b043-46a1-b901-deaa2baade10/image.png)

3초뒤 캐시가 만료되어 다시 DB 에서 조회하는 것을 확인할 수 있습니다. 이렇게 캐시와 DB 의 데이터 일관성을 유지하는 것이 중요합니다.

로컬 캐시를 통해 캐싱을 적용해보았지만, 대규모 트래픽 환경에서는 보통 서버의 성능을 향상시키기 위해 `Scale-up` 또는 `Scale-out` 방법을 사용합니다.

 - `Scale-up`은 단일 서버의 성능을 향상시키는 방법이고,
 - `Scale-out`은 여러 서버를 사용하여 부하를 분산시키는 방법입니다.

대다수의 서비스에서는 `Scale-out`을 통해 서버의 부하를 분산시키는 방법을 사용합니다. 그러나 여러 애플리케이션 서버가 띄워지는 경우, 로컬 캐시는 각 서버마다 별도로 관리되기 때문에 캐시의 일관성을 유지하기 어렵습니다.

**따라서 이때는 분산 캐시를 사용하여 여러 서버 간에 캐시를 공유하고 일관성을 유지할 수 있습니다. 이를 위해 Redis를 사용하면 여러 대의 서버가 공유하는 캐시 서버를 바라보기 때문에 캐시의 일관성을 유지할 수 있습니다.**

---

# Redis 캐싱

Spring의 `CacheManager` 추상화는 다양한 캐시 구현체를 쉽게 교체할 수 있도록 설계되어 있습니다. 이를 통해 로컬 캐시에서 Redis 캐시로 전환하는 작업이 상대적으로 간단합니다. 기본적으로 Spring은 여러 가지 CacheManager 구현체를 제공하며, 이를 통해 로컬 메모리 캐시, Redis 캐시, Ehcache, Caffeine 등을 사용할 수 있습니다.

```kotlin
@Bean
fun redisCacheManager(redisConnectionFactory: RedisConnectionFactory) : RedisCacheManager {
   val config  = RedisCacheConfiguration.defaultCacheConfig() // redis 캐시의 기본 설정
      .entryTtl(Duration.ofSeconds(3)) // 캐시의 만료 시간을 3초로 설정
      .disableCachingNullValues() // null 값은 캐시하지 않음
      .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())) // key 는 StringRedisSerializer 로 직렬화
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())) // value 는 GenericJackson2JsonRedisSerializer 로 직렬화

   return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build()
}
```

위와 같이 `RedisCacheManager` 빈을 등록하면 Redis 캐시를 사용할 수 있습니다. 이제 `@Cacheable` 어노테이션을 사용하여 캐싱을 적용할 수 있습니다.
기존 로컬 캐시와 똑같은 방법으로 캐싱을 적용할 수 있습니다.

**구현 및 테스트는 위 로컬 캐시와 동일합니다. (캐시 구현체만 변경하여 Redis를 사용하도록 DI 했기때문에)**

**Redis 캐싱 실행 결과**

![](https://velog.velcdn.com/images/asdcz11/post/88f12799-4dac-4b34-ad89-3741f776a383/image.png)

concerts 캐시를 삭제하는 스케줄러 로직은 삭제 후 마지막 3번째 테스트만 다시 실행해본 결과 캐시 만료시간 3초 설정이 적용되어 동일하게 테스트가 진행된걸 확인 할 수 있었습니다.

이렇게 Redis 를 사용하여 캐싱을 적용하면 여러 서버 간에 캐시를 공유하고 일관성을 유지할 수 있습니다.

---

# 콘서트 대기열 Redis 구현

콘서트 대기열은 콘서트 예약 시스템에서 사용자가 콘서트 티켓을 예약할 때, 예약 대기열에 추가되는 기능입니다.

현재는 Redis 없이 DB만을 사용해서 콘서트 대기열을 구현하고 있습니다. 사용자는 콘서트 좌석 예매 API를 사용하기 전에 토큰을 발급받아야 하며, 이 토큰은 DB에 저장됩니다. 스케줄러는 1분마다 DB에 저장된 토큰을 검사하여, 해당 토큰의 상태를 Active로 변경합니다. 변경된 토큰만 정상적으로 API를 사용하도록 구현되어 있습니다.

하지만 이러한 방식은 DB에서 토큰을 조회하고 상태를 변경하는 작업이 매우 빈번하게 발생하여, DB에 부하를 주게 됩니다. 실제로 스케줄러가 1분마다 실행되면서 토큰 상태를 검증하고 변경하는 작업이 이루어지고 있습니다.

따라서 이러한 토큰 관리 작업을 메모리 기반인 Redis로 처리하면 빠른 속도로 대기열을 처리할 수 있고,
비즈니스 로직 외의 토큰 관리를 Redis에서 처리함으로써 DB는 비즈니스 로직과 관련된 데이터만 처리하도록 할 수 있습니다.

제 설계는 다음과 같습니다

1. 사용자가 토큰을 발급 받고 Redis 에 토큰을 `WaitingQueue` 에 저장합니다. 이때는 요청이 들어온 순서대로 토큰을 저장해야하므로 Redis 의 `sorted set` 을 사용합니다.
2. 이후 N분마다 M개의 `WaitingQueue` 을 `Actice Token` 으로 변경하고 `ActiveQueue` 에 저장합니다.
3. 사용자는 `ActiveQueue` 에서 토큰을 조회하여 API 를 사용할 수 있습니다.

**위 방법의 장점으로는 대기열 고객에게 서비스 진입 가능 시간을 대체로 보장할수 있지만, 
단점으로는 서비스를 이용하는 유저의 수가 보장 될 수 없습니다.**

### WaitingQueue 구현

```kotlin
     fun generateToken(user: UserEntity): String { 
         // 토큰이 존재 한다면 기존 토큰 리턴
        val userKey = generateUserKey(user.id)
        val existingToken = redisTemplate.opsForValue().get(userKey)
   
        return existingToken ?: run {
            val token = encodeUserId()
            val userJson = objectMapper.writeValueAsString(user)
            val currentTime = System.currentTimeMillis().toDouble()

            // 토큰이 없다면 WAITING_QUEUE 에 등록 후 토큰 발급
            redisTemplate.opsForZSet().add(WAITING_QUEUE, userJson, currentTime)
            redisTemplate.opsForValue().set(userKey, token, 1, TimeUnit.HOURS)
            token
        }
    }
```

기존에 발급 받은 토큰이 존재한다면 기존 토큰을 반환합니다. 

기존에 발급 받은 토큰이 없다면 `WAITING_QUEUE` 에 유저 정보를 등록하고, 토큰을 발급합니다.

### WaitingQueue 에서 ActiveQueue 로 이동

```kotlin

```

### ActiveQueue 구현

```kotlin
```

이후 Interceptor 에서 토큰을 검증할때 기존에는 DB 로 조회쿼리를 매번 날렸지만
Redis 를 사용한 지금은 ActiveQueue 애 토큰이 있는지만 확인하면 되므로 매우 빠른 속도로 토큰을 검증할 수 있습니다.

### 정리하며...

대기열 시스템을 DB 만을 사용해서 구현했을때 접했던 문제점들을 Redis 를 사용하여 개선 할 수 있었습니다.

처음부터 Redis 를 사용하는게 아닌 DB 로 구현해보고 문제점을 파악하고 Redis 를 사용하여 개선하는 방법을 사용하면서
장단점을 파악할 수 있었습니다.