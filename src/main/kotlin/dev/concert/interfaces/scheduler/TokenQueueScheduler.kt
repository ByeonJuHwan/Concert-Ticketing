package dev.concert.interfaces.scheduler

import dev.concert.application.token.TokenFacade
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenQueueScheduler (
    private val tokenFacade: TokenFacade,
) {

    /**
     * 토큰을 Waiting Queue 에서 Active Queue 로 이동시키는 스케줄러
     */
    @Scheduled(fixedRate = 10000) // 10초 마다 실행
    fun tokenScheduler() {
        tokenFacade.manageTokenStatus()
    }
}
