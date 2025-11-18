package org.ktor_lecture.paymentservice.adapter.out.api

import org.ktor_lecture.paymentservice.application.port.out.ReservationApiClient
import org.springframework.stereotype.Component

@Component
class ReservationApiClientImpl(

): ReservationApiClient {


    override fun getReservation(reservationId: Long) {
        TODO("Not yet implemented")
    }
}