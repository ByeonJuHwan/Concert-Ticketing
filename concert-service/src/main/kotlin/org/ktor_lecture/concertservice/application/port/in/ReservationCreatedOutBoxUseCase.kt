package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent

interface ReservationCreatedOutBoxUseCase {
    fun handleReservationCreatedOutBox(event: ReservationCreatedEvent)
}