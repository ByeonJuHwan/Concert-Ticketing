package org.ktor_lecture.concertservice.application.port.`in`

interface ReservationCreatedEventRetryUseCase {
    fun retryReservationCreatedEvent()
}