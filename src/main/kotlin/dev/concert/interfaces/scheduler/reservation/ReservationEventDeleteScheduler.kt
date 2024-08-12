package dev.concert.interfaces.scheduler.reservation

import dev.concert.application.reservation.ReservationFacade
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReservationEventDeleteScheduler (
    private val reservationFacade: ReservationFacade,
) {
    /**
     * 예약 아웃박스의 데이터는 3일이 지나면 자동으로 삭제된다
     */
    @Scheduled(cron = "0 0 0 */3 * ?")
    fun reservationEventRetryScheduler() {
        reservationFacade.deleteOutBoxEvents()
    }
}