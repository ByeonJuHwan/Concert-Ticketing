package dev.concert.domain.service.token

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.application.token.dto.TokenValidationResult
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import dev.concert.domain.util.consts.ACTIVE_QUEUE
import dev.concert.domain.util.consts.WAITING_QUEUE
import dev.concert.domain.util.lock.LockKeyGenerator
import dev.concert.util.Base64Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Primary
class TokenRedisServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val lockKeyGenerator: LockKeyGenerator,
) : TokenService {

    private val log : Logger = LoggerFactory.getLogger(TokenRedisServiceImpl::class.java)

    private val objectMapper : ObjectMapper = jacksonObjectMapper()

    override fun generateToken(user: UserEntity): String {
        val userKey = generateUserKey(user.id)
        val existingToken = redisTemplate.opsForValue().get(userKey)

        return existingToken ?: run {
            val token = encodeUserId()
            val userJson = objectMapper.writeValueAsString(user)
            val currentTime = System.currentTimeMillis().toDouble()

            redisTemplate.opsForZSet().add(WAITING_QUEUE, userJson, currentTime)
            redisTemplate.opsForValue().set(userKey, token, 1, TimeUnit.HOURS)
            token
        }
    }

    override fun getToken(user: UserEntity): TokenResponseDto {
        val userKey = generateUserKey(user.id)
        val userJson = objectMapper.writeValueAsString(user)
        val remainingTime = redisTemplate.getExpire(userKey, TimeUnit.SECONDS)
        val token = redisTemplate.opsForValue().get(userKey) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)

        // 큐에서 상태 확인
        val status = checkQueueTokenStatus(userJson)

        // 순서 조회
        val queueOrder = if (status == QueueTokenStatus.WAITING) {
            redisTemplate.opsForZSet().rank(WAITING_QUEUE, userJson)?.toInt() ?: -1
        } else {
            0
        }

        return TokenResponseDto(
            token = token,
            status = status,
            queueOrder = queueOrder,
            remainingTime = remainingTime
        )
    }

    override fun deleteToken(user: UserEntity) {
        val token = (redisTemplate.opsForValue()
            .get(generateUserKey(user.id))
            ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND))
        redisTemplate.opsForSet().remove(ACTIVE_QUEUE, token)
        redisTemplate.delete(generateUserKey(user.id))
        log.info("토큰 삭제 완료")
    }

    /**
     * user - token 에서 token 을 가져와서
     * wait_queue 에서 active_queue 로 이동
     * 1분마다 10 명씩 이동
     */
    override fun manageTokenStatus() {
        val tokenList = redisTemplate.opsForZSet().range(WAITING_QUEUE, 0, 9)
        tokenList?.forEach { userJson ->
            val user: UserEntity = objectMapper.readValue(userJson)
            val userKey = generateUserKey(user.id)
            val token = redisTemplate.opsForValue().get(userKey) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)
            redisTemplate.opsForSet().add(ACTIVE_QUEUE, token)
            redisTemplate.opsForZSet().remove(WAITING_QUEUE, userJson)
        }
    }

    override fun manageExpiredTokens() {
        TODO("Not yet implemented")
    }

    override fun validateToken(token: String): TokenValidationResult {
        redisTemplate.op


    }

    private fun encodeUserId() : String {
        val uuid = UUID.randomUUID().toString()
        val timeStamp = System.currentTimeMillis().toString()
        return Base64Util.encode((uuid + timeStamp).toByteArray())
    }

    private fun checkQueueTokenStatus(userJson: String): QueueTokenStatus {
        val status = when {
            redisTemplate.opsForZSet().rank(ACTIVE_QUEUE, userJson) != null -> QueueTokenStatus.ACTIVE
            redisTemplate.opsForZSet().rank(WAITING_QUEUE, userJson) != null -> QueueTokenStatus.WAITING
            else -> throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)
        }
        return status
    }

    /**
     * 사용자 ID로 유저 키 생성
     */
    private fun generateUserKey(userId: Long): String {
        return lockKeyGenerator.generateLockKeyWithPrefix(prefix = "user", param = userId)
    }
}
