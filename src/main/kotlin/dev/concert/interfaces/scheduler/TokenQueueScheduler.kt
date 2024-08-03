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

    /**
     * Active Queue 에서 결제까지 진행하지 않고 이탈한 토큰 삭제
     * 1일에 1번 실행 (매일 자정에 실행)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun deleteActiveTokenExpired() {
        tokenFacade.manageExpiredTokens()
    }
}