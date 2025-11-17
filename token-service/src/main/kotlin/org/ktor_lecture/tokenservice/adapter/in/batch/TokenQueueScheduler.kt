package org.ktor_lecture.tokenservice.adapter.`in`.batch

import org.ktor_lecture.tokenservice.application.port.`in`.MoveTokenToActiveQueueUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenQueueScheduler (
    private val moveTokenToActiveQueueUseCase: MoveTokenToActiveQueueUseCase,
) {

    /**
     * 토큰을 Waiting Queue 에서 Active Queue 로 이동시키는 스케줄러
     */
    @Scheduled(fixedRate = 10000) // 10초 마다 실행
    fun tokenScheduler() {
        moveTokenToActiveQueueUseCase.moveTokenToActiveQueue()
    }
}