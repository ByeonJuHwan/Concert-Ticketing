package dev.concert.domain.service.token

import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.application.token.dto.TokenValidationResult
import dev.concert.domain.repository.TokenRepository
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import dev.concert.util.Base64Util
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class TokenServiceImpl (
    private val tokenRepository: TokenRepository,
) : TokenService {

    @Transactional
    override fun generateToken(user : UserEntity): String {
        tokenRepository.deleteByUser(user)

        return encodeUserId().also { token ->
            tokenRepository.saveToken(queueTokenEntity(user, token))
        }
    }

    @Transactional(readOnly = true)
    override fun getToken(token: String): TokenResponseDto {
        val queueToken = getQueueToken(token)

        return TokenResponseDto(
            queueOrder = getQueueOrder(queueToken),
            remainingTime = Duration.between(LocalDateTime.now(), queueToken.expiresAt).seconds,
            token = queueToken.token,
            status = queueToken.status,
        )
    }

    @Transactional
    override fun deleteToken(user: UserEntity) {
        tokenRepository.deleteByUser(user)
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

    @Transactional
    override fun validateToken(token: String): TokenValidationResult {
        val tokenEntity = tokenRepository.findByToken(token)

        return when {
            tokenEntity == null -> TokenValidationResult.INVALID
            tokenEntity.isExpired() -> TokenValidationResult.EXPIRED
            !tokenEntity.isAvailable() -> TokenValidationResult.NOT_AVAILABLE
            else -> TokenValidationResult.VALID
        }
    }

    @Transactional
    override fun manageExpiredTokens() {
        tokenRepository.deleteExpiredTokens()
    }

    private fun getQueueToken(token: String) =
        tokenRepository.findByToken(token) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)

    private fun queueTokenEntity(
        user: UserEntity,
        token: String,
    ) = QueueTokenEntity(
        user = user,
        token = token,
    )

    private fun getQueueOrder(queueToken:QueueTokenEntity) : Int {
        val firstQueueId = tokenRepository.findFirstQueueOrderId()
        return if(firstQueueId == 0L) 0 else (queueToken.id - firstQueueId).toInt() + 1
    }

    private fun encodeUserId() : String {
        val uuid = UUID.randomUUID().toString()
        val timeStamp = System.currentTimeMillis().toString()
        return Base64Util.encode((uuid + timeStamp).toByteArray())
    }
}

