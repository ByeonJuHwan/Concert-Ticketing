package org.ktor_lecture.concertservice.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration
import java.util.*
import java.util.stream.Collectors

@Configuration
class LocalCacheConfig {

    @Primary
    @Bean
    fun localCacheManager(): SimpleCacheManager {
        val caches = Arrays.stream(CacheNames.values())
            .map { cache ->
                CaffeineCache(
                    cache.cacheName, Caffeine.newBuilder()
                        .recordStats()
                        .expireAfterWrite(cache.expiredAfterWrite)
                        .maximumSize(cache.maximumSize)
                        .build()
                )
            }
            .collect(Collectors.toList())

        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(caches)

        return cacheManager
    }
}

enum class CacheNames(val cacheName: String, val expiredAfterWrite: Duration, val maximumSize: Long) {
    META_CACHE("MetaCache", Duration.ofHours(1), 1000);
}