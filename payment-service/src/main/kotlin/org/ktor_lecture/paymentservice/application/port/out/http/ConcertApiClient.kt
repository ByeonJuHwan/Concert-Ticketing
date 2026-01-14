package org.ktor_lecture.paymentservice.application.port.out.http

import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse

interface ConcertApiClient {
    fun getReservation(reservationId: Long): ConcertReservationResponse
    fun reservationExpiredAndSeatAvaliable(reservationId: Long)
    fun changeReservationPaid(reservationId: Long)
    fun changeSeatReserved(reservationId: Long)
    fun changeReservationPending(reservationId: Long)
    fun changeSeatTemporarilyAssigned(reservationId: Long)

}