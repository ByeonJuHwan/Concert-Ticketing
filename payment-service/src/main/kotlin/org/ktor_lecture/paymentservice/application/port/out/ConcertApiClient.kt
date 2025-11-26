package org.ktor_lecture.paymentservice.application.port.out

import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse

interface ConcertApiClient {
    fun getReservation(reservationId: Long): ConcertReservationResponse
    fun reservationExpiredAndSeatAvaliable(reservationId: Long)
    fun changeReservationPaid(requestId: String)
    fun changeSeatReserved(requestId: String)
    fun changeReservationPending(requestId: String)
    fun changeSeatTemporarilyAssigned(requestId: String)

}