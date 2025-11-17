package org.ktor_lecture.tokenservice.adapter.`in`.batch

import org.ktor_lecture.tokenservice.application.port.`in`.ManageExpiredActiveTokenUseCase
import org.ktor_lecture.tokenservice.application.port.`in`.MoveTokenToActiveQueueUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenQueueScheduler (
    private val moveTokenToActiveQueueUseCase: MoveTokenToActiveQueueUseCase,
    private val manageExpiredActiveTokenUseCase: ManageExpiredActiveTokenUseCase,
) {

    /**
     * 토큰을 Waiting Queue 에서 Active Queue 로 이동시키는 스케줄러
     */
    @Scheduled(fixedRate = 10000) // 10초 마다 실행
    fun tokenScheduler() {
        moveTokenToActiveQueueUseCase.moveTokenToActiveQueue()
    }

    /**
     * Active Queue 에서 결제까지 진행하지 않고 이탈한 토큰 삭제
     * 1일에 1번 실행 (매일 자정에 실행)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun deleteActiveTokenExpired() {
        manageExpiredActiveTokenUseCase.deleteExpiredActiveTokens()
    }
}