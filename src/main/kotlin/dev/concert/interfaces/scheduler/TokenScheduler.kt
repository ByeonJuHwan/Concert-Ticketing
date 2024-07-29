package dev.concert.interfaces.scheduler

import dev.concert.application.token.TokenFacade
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

//@Component
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

    /**
     * 만료된 토큰을 삭제하는 스케줄러
     * 1일에 1번 실행 (매일 자정에 실행)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun deleteExpiredTokens() {
        tokenFacade.manageExpiredTokens()
    }
} 
