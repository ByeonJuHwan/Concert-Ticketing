package dev.concert.application.reservation

import dev.concert.domain.event.reservation.ReservationEvent
import dev.concert.domain.service.reservation.ReservationService
import org.springframework.stereotype.Service

@Service
class ReservationFacade (
    private val reservationService: ReservationService,
) {

    fun recordReservationOutBoxMsg(event : ReservationEvent) {
        reservationService.saveMsgToOutBox(event)
    }

    fun retryEvents() {
        reservationService.retryInitOrFailEvents()
    }

    fun deleteOutBoxEvents() {
        reservationService.deleteOutBoxEvents()
    }

    fun publishReservationEvent(event: ReservationEvent) {
        reservationService.publishReservationEvent(event)
    }
}