package dev.concert.application.data

import dev.concert.domain.service.data.DataPlatformService
import dev.concert.domain.service.reservation.ReservationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataPlatformFacade (
    private val dataPlatformService : DataPlatformService,
    private val reservationService : ReservationService,
) {

    @Transactional
    fun sendReservationData(reservationId : Long) {
        val reservation = reservationService.getReservation(reservationId)
        dataPlatformService.sendReservationData(reservation)
    }
}