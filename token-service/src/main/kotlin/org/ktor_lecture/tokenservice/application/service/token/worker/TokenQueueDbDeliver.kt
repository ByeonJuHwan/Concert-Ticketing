package org.ktor_lecture.tokenservice.application.service.token.worker

import io.lettuce.core.KillArgs.Builder.user
import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("DB")
class TokenQueueDbDeliver (
    @Qualifier("DB") private val tokenRepository: TokenRepository,
    @Qualifier("DB") private val lockKeyGenerator: LockKeyGenerator,
): TokenQueueDeliver {

    private val log : Logger = LoggerFactory.getLogger(this::class.java)

    override fun deliverWaitingToActiveQueue() {
        val tokenList = tokenRepository.findTopWaitingQueueEntities(0, 10)

        tokenList.forEach { waitingQueueEntity ->
            runCatching {
                val key = lockKeyGenerator.generateLockKeyWithPrefix(prefix = "token-user", value = waitingQueueEntity.queueTokenUser.id.toString())
                val token = tokenRepository.findTokenById(key) ?: throw ConcertException(ErrorCode.TOKEN_NOT_FOUND)
                tokenRepository.addActiveQueueEntity(waitingQueueEntity.queueTokenUser, token)
                tokenRepository.removeWaitingQueueTokenEntity(waitingQueueEntity)
            }.onFailure { e ->
                log.error("WaitingQueue -> ActiveQueue Error : ${waitingQueueEntity.id}", e)
            }
        }
    }
}