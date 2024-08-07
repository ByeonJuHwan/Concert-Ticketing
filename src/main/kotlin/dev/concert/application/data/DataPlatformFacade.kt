package dev.concert.application.data

import dev.concert.domain.service.data.DataPlatformService
import dev.concert.domain.service.reservation.ReservationService
import org.springframework.stereotype.Service

@Service
class DataPlatformFacade (
    private val dataPlatformService : DataPlatformService,
    private val reservationService : ReservationService,
) {

    fun sendReservationData(reservationId : Long) {
        val reservation = reservationService.getReservation(reservationId)
        dataPlatformService.sendReservationData(reservation)
    }
}