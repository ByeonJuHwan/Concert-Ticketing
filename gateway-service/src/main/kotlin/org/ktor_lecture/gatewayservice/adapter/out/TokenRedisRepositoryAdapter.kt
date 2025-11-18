package org.ktor_lecture.gatewayservice.adapter.out

import org.ktor_lecture.gatewayservice.application.port.out.TokenRepository
import org.ktor_lecture.gatewayservice.domain.status.TokenValidationResult
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

const val ACTIVE_QUEUE = "active_queue"

@Component
class TokenRedisRepositoryAdapter (
    private val redisTemplate: RedisTemplate<String, String>,
): TokenRepository {

    override fun validateToken(token: String): TokenValidationResult {
        val hasQueue = redisTemplate.opsForSet().isMember(ACTIVE_QUEUE, token)

        return if (hasQueue == true) {
            TokenValidationResult.VALID
        } else {
            TokenValidationResult.NOT_AVAILABLE
        }
    }
}