package dev.concert.application.token

import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.domain.TokenRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import dev.concert.exception.TokenNotFoundException
import dev.concert.exception.UserNotFountException
import dev.concert.util.Base64Util
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service 
class TokenServiceImpl ( 
    private val tokenRepository: TokenRepository, 
    private val userRepository: UserRepository, 
) : TokenService { 
 
    @Transactional 
    override fun generateToken(userId: Long): String { 
        val user = getUser(userId) 
 
        tokenRepository.deleteToken(user) 
 
        val token = encodeUserId() 

        val queueToken = queueTokenEntity(user, token) 

        tokenRepository.saveToken(queueToken) 

        return token 
    } 

    @Transactional(readOnly = true) 
    override fun getToken(token: String): TokenResponseDto { 
        val queueToken = getQueueToken(token) 

        val remainingTime = Duration.between(LocalDateTime.now(), queueToken.expiresAt).seconds 

        return TokenResponseDto( 
            queueOrder = getQueueOrder(queueToken), 
            remainingTime = remainingTime, 
            token = queueToken.token, 
            status = queueToken.status, 
        ) 
    } 

    @Transactional
    override fun isTokenExpired(token: String): Boolean {
        val currentToken = tokenRepository.findByToken(token) ?: return true
        if(currentToken.expiresAt.isBefore(LocalDateTime.now())){
            currentToken.changeStatusExpired()
            return true
        }
        return false
    }

    @Transactional
    override fun deleteToken(user: UserEntity) {
        tokenRepository.deleteToken(user)
    }

    /**
     *  토큰 상태가 waiting, Active 인 토큰들을 30명 제한으로 Active 로 변경
     *  토큰 상태가 Active 인 토큰들 중 만료시간이 지나면 Expired 로 변경
     */
    @Transactional
    override fun manageTokenStatus() {
        val availableTokens = tokenRepository.findWaitingAndActiveTokens()

        availableTokens.forEach {
            if (it.status == QueueTokenStatus.WAITING){
                it.changeStatusToActive()
            }
            if (it.status == QueueTokenStatus.ACTIVE && LocalDateTime.now().isAfter(it.expiresAt)) {
                it.changeStatusExpired()
            }
        }
    }

    @Transactional(readOnly = true) 
    override fun isAvailableToken(token: String): Boolean { 
        val queueToken = getQueueToken(token) 
        return queueToken.status == QueueTokenStatus.ACTIVE 
    } 

    private fun getQueueToken(token: String) = 
        tokenRepository.findByToken(token) ?: throw TokenNotFoundException("토큰이 존재하지 않습니다") 

    private fun queueTokenEntity( 
        user: UserEntity, 
        token: String, 
    ) = QueueTokenEntity( 
        user = user, 
        token = token, 
    ) 

    private fun getQueueOrder(queueToken:QueueTokenEntity) : Int { 
        val firstQueueId = tokenRepository.findFirstQueueOrderId() 
        if(firstQueueId == 0L) return 0 
        return (queueToken.id - firstQueueId).toInt() + 1 
    } 

    private fun encodeUserId() : String { 
        val uuid = UUID.randomUUID().toString() 
        val timeStamp = System.currentTimeMillis().toString() 
        return Base64Util.encode((uuid + timeStamp).toByteArray()) 
    } 

    private fun getUser(userId: Long) = 
        userRepository.findById(userId) ?: throw UserNotFountException("존재하는 회원이 없습니다") 
}

