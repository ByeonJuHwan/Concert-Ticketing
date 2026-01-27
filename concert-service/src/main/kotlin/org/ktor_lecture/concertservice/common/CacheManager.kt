package org.ktor_lecture.concertservice.common

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Component

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

enum class CacheType{
    LOCAL, REDIS
}

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

enum class ActionType {
    CREATE,
    UPDATE,
    DELETE
}