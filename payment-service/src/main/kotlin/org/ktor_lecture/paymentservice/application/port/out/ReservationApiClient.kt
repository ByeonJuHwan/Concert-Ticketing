package org.ktor_lecture.paymentservice.application.port.out

interface ReservationApiClient {
    fun getReservation(reservationId: Long)

}