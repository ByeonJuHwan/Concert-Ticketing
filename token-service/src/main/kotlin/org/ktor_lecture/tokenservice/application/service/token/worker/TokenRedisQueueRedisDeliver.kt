package org.ktor_lecture.tokenservice.application.service.token.worker

import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.common.JsonUtil
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("REDIS")
class TokenRedisQueueRedisDeliver (
    @Qualifier("REDIS") private val tokenRepository: TokenRepository,
    @Qualifier("REDIS") private val lockKeyGenerator: LockKeyGenerator,
): TokenQueueDeliver {

    private val log : Logger = LoggerFactory.getLogger(this::class.java)

    override fun deliverWaitingToActiveQueue() {
        val tokenList: Set<String> = tokenRepository.findTopWaitingTokens(0, 10) ?: return

        tokenList.forEach { userJson ->
            runCatching {
                val user = JsonUtil.decodeFromJson<QueueTokenUserEntity>(userJson)
                val key = lockKeyGenerator.generateLockKeyWithPrefix(prefix = "token-user", value = user.id.toString())
                val token = tokenRepository.findTokenById(key) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)
                tokenRepository.addActiveQueue(token)
                tokenRepository.removeWaitingQueueToken(userJson)
            }.onFailure { e ->
                log.error("WaitingQueue -> ActiveQueue Error : $userJson", e)
            }
        }
    }
}