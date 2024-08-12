package dev.concert.interfaces.scheduler.reservation

import dev.concert.application.reservation.ReservationFacade
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReservationEventRetryScheduler (
    private val reservationFacade: ReservationFacade,
) {

    /**
     * 이벤트의 상태가 SEND_SUCCESS 가 아니면서,
     * CREATED_AT 이 현 시간 기준으로 10분 이상 넘어간 이벤트 재시도
     */
    @Scheduled(cron = "0 0 0 */3 * ?")
    fun reservationEventRetryScheduler() {
        reservationFacade.retryEvents()
    }
}