package dev.concert.infrastructure.redis


import dev.concert.domain.repository.QueueTokenRepository
import dev.concert.domain.util.consts.ACTIVE_QUEUE
import dev.concert.domain.util.consts.WAITING_QUEUE
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit
 
@Repository 
class TokenRedisRepositoryImpl( 
    private val redisTemplate: RedisTemplate<String, String>, 
) : QueueTokenRepository { 
 
    override fun findTokenByKey(key: String): String? { 
        return redisTemplate.opsForValue().get(key) 
    } 
 
    override fun addWaitingQueue(userJson: String, currentTime: Double) { 
        redisTemplate.opsForZSet().add(WAITING_QUEUE, userJson, currentTime) 
    } 
 
    override fun createToken(userKey: String, token: String) { 
        redisTemplate.opsForValue().set(userKey, token, 1, TimeUnit.HOURS) 
    } 

    override fun getTokenExpireTime(userKey: String): Long? { 
        return redisTemplate.getExpire(userKey, TimeUnit.SECONDS) 
    } 

    override fun deleteTokenActiveQueue(token: String) { 
        redisTemplate.opsForSet().remove(ACTIVE_QUEUE, token) 
    } 

    override fun deleteToken(userKey: String) { 
        redisTemplate.delete(userKey) 
    } 

    override fun findTopWaitingTokens(start: Long, end: Long): Set<String>? { 
        return redisTemplate.opsForZSet().range(WAITING_QUEUE, start, end) 
    } 

    override fun addActiveQueue(token: String) { 
        redisTemplate.opsForSet().add(ACTIVE_QUEUE, token) 
    } 

    override fun removeWaitingQueueToken(userJson: String) { 
        redisTemplate.opsForZSet().remove(WAITING_QUEUE, userJson) 
    } 

    override fun isTokenInActiveQueue(token: String): Boolean {
        return redisTemplate.opsForSet().isMember(ACTIVE_QUEUE, token) ?: false
    } 

    override fun getRankInWaitingQueue(userJson: String): Long? { 
        return redisTemplate.opsForZSet().rank(WAITING_QUEUE, userJson) 
    }

    override fun deleteAllActiveTokens() : Boolean {
        return redisTemplate.delete(ACTIVE_QUEUE)
    }
}
