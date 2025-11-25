package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertReservationResponse

interface SearchReservationUseCase {
    fun getReservation(reservationId: Long): ConcertReservationResponse
}