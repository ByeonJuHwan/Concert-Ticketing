package dev.concert.domain.service.reservation.publisher

import dev.concert.domain.service.reservation.event.ReservationSuccessEvent

interface ReservationEventPublisher {
    fun publish(event : ReservationSuccessEvent)
}