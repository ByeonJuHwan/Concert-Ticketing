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
import dev.concert.domain.repository.QueueTokenRepository
import dev.concert.domain.util.lock.LockKeyGenerator
import dev.concert.util.Base64Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.*
 
@Service 
@Primary 
class TokenRedisServiceImpl( 
    private val queueTokenRepository: QueueTokenRepository, 
    private val lockKeyGenerator: LockKeyGenerator, 
) : TokenService { 
 
    private val log : Logger = LoggerFactory.getLogger(TokenRedisServiceImpl::class.java) 
 
    private val objectMapper : ObjectMapper = jacksonObjectMapper() 
 
    override fun generateToken(user: UserEntity): String { 
        val userKey = generateUserKey(user.id) 
        val existingToken = queueTokenRepository.findTokenByKey(userKey) 
 
        return existingToken ?: run { 
            val token = encodeUserId() 
            val userJson = objectMapper.writeValueAsString(user) 
            val currentTime = System.currentTimeMillis().toDouble() 
 
            queueTokenRepository.addWaitingQueue(userJson, currentTime) 
            queueTokenRepository.createToken(userKey, token) 
            token 
        } 
    } 
 
    override fun getToken(user: UserEntity): TokenResponseDto { 
        val userKey = generateUserKey(user.id) 
        val userJson = objectMapper.writeValueAsString(user) 
        val remainingTime = queueTokenRepository.getTokenExpireTime(userKey) ?: 0 
        val token = queueTokenRepository.findTokenByKey(userKey) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND) 
 
        // 큐에서 상태 확인 
        val status = checkQueueTokenStatus(userJson, token) 
 
        // 순서 조회 
        val queueOrder = calcQueueOrder(status, userJson) 
 
        return TokenResponseDto( 
            token = token, 
            status = status, 
            queueOrder = queueOrder, 
            remainingTime = remainingTime 
        ) 
    } 


    override fun deleteToken(user: UserEntity) { 
        val token = queueTokenRepository.findTokenByKey(generateUserKey(user.id)) 
            ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND) 
        queueTokenRepository.deleteTokenActiveQueue(token) 
        queueTokenRepository.deleteToken(generateUserKey(user.id)) 
        log.info("토큰 삭제 완료") 
    }

    /**
     * user - token 에서 token 을 가져와서
     * wait_queue 에서 active_queue 로 이동
     * 10초마다 1000 개씩 이동
     */
    override fun manageTokenStatus() { 
        val tokenList = queueTokenRepository.findTopWaitingTokens(0, 1999) 
        tokenList?.forEach { userJson -> 
            runCatching { 
                val user: UserEntity = objectMapper.readValue(userJson) 
                val userKey = generateUserKey(user.id) 
                val token = queueTokenRepository.findTokenByKey(userKey) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND) 
                queueTokenRepository.addActiveQueue(token) 
                queueTokenRepository.removeWaitingQueueToken(userJson) 
            }.onFailure { e-> 
                log.error("WaitingQueue -> ActiveQueue Error : $userJson", e) 
            } 
        } 
    } 
  
    override fun manageExpiredTokens() {
        TODO("Not yet implemented")
    }

    override fun validateToken(token: String): TokenValidationResult { 
        return  when { 
            !queueTokenRepository.isTokenInActiveQueue(token) -> TokenValidationResult.NOT_AVAILABLE 
            else -> TokenValidationResult.VALID 
        } 
    } 

    private fun encodeUserId() : String { 
        val uuid = UUID.randomUUID().toString() 
        val timeStamp = System.currentTimeMillis().toString() 
        return Base64Util.encode((uuid + timeStamp).toByteArray()) 
    } 

    private fun checkQueueTokenStatus(userJson: String, token: String): QueueTokenStatus { 
        return when { 
            queueTokenRepository.isTokenInActiveQueue(token) -> QueueTokenStatus.ACTIVE 
            queueTokenRepository.getRankInWaitingQueue(userJson) != null -> QueueTokenStatus.WAITING 
            else -> throw ConcertException(ErrorCode.TOKEN_NOT_FOUND) 
        } 
    } 
    private fun calcQueueOrder(status: QueueTokenStatus, userJson: String) = 
        if (status == QueueTokenStatus.WAITING) { 
            queueTokenRepository.getRankInWaitingQueue(userJson)?.toInt() ?: -1 
        } else { 
            0 
        } 
 
    /**
     * 사용자 ID로 유저 키 생성
     */
    private fun generateUserKey(userId: Long): String { 
        return lockKeyGenerator.generateLockKeyWithPrefix(prefix = "user", param = userId) 
    } 
} 
