package dev.concert.application.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class RedisLockManager (
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun lock(key: Long): String? {
        val value = UUID.randomUUID().toString()
        val lockAcquired = redisTemplate
            .opsForValue()
            .setIfAbsent(generateKey(key), value, Duration.ofMillis(3_000))

        return if (lockAcquired == true) value else null
    }

    fun unlock(key: Long, value: String): Boolean {
        val redisKey = generateKey(key)
        val currentValue = redisTemplate.opsForValue().get(redisKey)
        if (currentValue == value) {
            return redisTemplate.delete(redisKey)
        }
        return false
    }

    private fun generateKey(key: Long): String = "lock:seat:$key"
}