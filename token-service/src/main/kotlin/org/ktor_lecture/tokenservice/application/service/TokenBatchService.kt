package org.ktor_lecture.tokenservice.application.service

import org.ktor_lecture.tokenservice.application.port.`in`.ManageExpiredActiveTokenUseCase
import org.ktor_lecture.tokenservice.application.port.`in`.MoveTokenToActiveQueueUseCase
import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.common.JsonUtil
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TokenBatchService (
    private val tokenRepository: TokenRepository,
    private val lockKeyGenerator: LockKeyGenerator,
): MoveTokenToActiveQueueUseCase, ManageExpiredActiveTokenUseCase {

    private val log : Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * user - token 에서 token 을 가져와서
     * wait_queue 에서 active_queue 로 이동
     * 10초마다 1000 개씩 이동
     */
    override fun moveTokenToActiveQueue() {
        val tokenList: Set<String> = tokenRepository.findTopWaitingTokens(0, 999) ?: return

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

    /**
     * Active Queue 에서 결제까지 진행하지 않고 이탈한 토큰 삭제 (하루에 한번 실행)
     */
    override fun deleteExpiredActiveTokens() {
        if (tokenRepository.deleteAllActiveTokens()) {
            log.info("Active Queue 삭제 완료")
        } else {
            log.error("Active Queue 삭제 실패")
        }
    }
}