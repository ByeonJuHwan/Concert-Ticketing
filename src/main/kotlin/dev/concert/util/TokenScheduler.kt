package dev.concert.util

import dev.concert.application.token.TokenFacade
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenScheduler (
    private val tokenFacade: TokenFacade,
) {

    /**
     * 토큰의 상태를 관리하는 스케줄러
     */
    @Scheduled(fixedRate = 60000) // 1분 마다 실행
    fun tokenScheduler() {
        tokenFacade.manageTokenStatus()
    }
}