package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent

interface SendReservationCreatedUseCase {
    fun publishReservationCreatedEvent(event: ReservationCreatedEvent)
}