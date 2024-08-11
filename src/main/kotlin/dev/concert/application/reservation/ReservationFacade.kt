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

    fun changeReservationOutBoxStatusSendSuccess(reservationId: Long) {
        reservationService.chanceMsgStatusSuccess(reservationId)
    }

    fun changeReservationOutBoxStatusSendFail(reservationId: Long) {
        reservationService.chanceMsgStatusFail(reservationId)
    }
}