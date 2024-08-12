package dev.concert.interfaces.scheduler

import dev.concert.application.reservation.ReservationFacade
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReservationEventRetryScheduler (
    private val reservationFacade: ReservationFacade,
) {

    /**
     *
     */
    @Scheduled(fixedRate = 300000)
    fun reservationEventRetryScheduler() {
        reservationFacade.retryEvents()
    }
}