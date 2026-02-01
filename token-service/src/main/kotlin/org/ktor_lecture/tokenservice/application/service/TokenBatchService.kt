package org.ktor_lecture.tokenservice.application.service

import org.ktor_lecture.tokenservice.application.port.`in`.ManageExpiredActiveTokenUseCase
import org.ktor_lecture.tokenservice.application.port.`in`.MoveTokenToActiveQueueUseCase
import org.ktor_lecture.tokenservice.application.port.out.LockKeyGenerator
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.application.service.token.worker.TokenQueueDeliver
import org.ktor_lecture.tokenservice.common.JsonUtil
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.exception.ConcertException
import org.ktor_lecture.tokenservice.domain.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class TokenBatchService (
//    @Qualifier("DB") private val tokenRepository: TokenRepository,
    @Qualifier("DB") private val tokenQueueDeliver: TokenQueueDeliver,
): MoveTokenToActiveQueueUseCase, ManageExpiredActiveTokenUseCase {

    /**
     * user - token 에서 token 을 가져와서
     * wait_queue 에서 active_queue 로 이동
     * 10초마다 1000 개씩 이동
     */
    override fun moveTokenToActiveQueue() {
        tokenQueueDeliver.deliverWaitingToActiveQueue()
    }

    /**
     * Active Queue 에서 결제까지 진행하지 않고 이탈한 토큰 삭제 (하루에 한번 실행)
     */
    override fun deleteExpiredActiveTokens() {
//        tokenRepository.deleteAllActiveTokens()
    }
}