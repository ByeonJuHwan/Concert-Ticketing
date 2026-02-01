package org.ktor_lecture.tokenservice.adapter.out

import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.common.JsonUtil
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.entity.WaitingQueueEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

const val WAITING_QUEUE = "waiting_queue"
const val ACTIVE_QUEUE = "active_queue"

@Component
@Qualifier("REDIS")
class TokenRedisRepositoryAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
): TokenRepository {

    override fun findTokenById(key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }

    override fun addWaitingQueue(user: QueueTokenUserEntity, currentTime: Double) {
        val userJson = JsonUtil.encodeToJson(user)
        redisTemplate.opsForZSet().add(WAITING_QUEUE, userJson, currentTime)
    }

    override fun createToken(key: String, token: String) {
        redisTemplate.opsForValue().set(key, token, 1, TimeUnit.HOURS)
    }

    override fun getTokenExpireTime(key: String): Long? {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS)
    }

    override fun isTokenInActiveQueue(token: String): Boolean {
        return redisTemplate.opsForSet().isMember(ACTIVE_QUEUE, token) ?: false
    }

    override fun getRankInWaitingQueue(userJson: String): Long? {
        return redisTemplate.opsForZSet().rank(WAITING_QUEUE, userJson)
    }

    override fun findTopWaitingTokens(start: Long, end: Long): Set<String>? {
        return redisTemplate.opsForZSet().range(WAITING_QUEUE, start, end)
    }

    override fun addActiveQueue(token: String) {
        redisTemplate.opsForSet().add(ACTIVE_QUEUE, token)
    }

    override fun addActiveQueueEntity(user: QueueTokenUserEntity, token: String) {
        TODO("Not yet implemented")
    }

    override fun removeWaitingQueueToken(userJson: String) {
        redisTemplate.opsForZSet().remove(WAITING_QUEUE, userJson)
    }

    override fun removeWaitingQueueTokenEntity(entity: WaitingQueueEntity) {
        TODO("Not yet implemented")
    }

    override fun deleteToken(key: String) {
        redisTemplate.delete(key)
    }

    override fun saveToken(user: QueueTokenUserEntity, token: String) {
        TODO("Not yet implemented")
    }

    override fun findTopWaitingQueueEntities(start: Long, end: Long): List<WaitingQueueEntity> {
        TODO("Not yet implemented")
    }

    override fun deleteAllActiveTokens() {
        redisTemplate.delete(ACTIVE_QUEUE)
    }
}