package dev.concert.domain.event.reservation.publisher

import dev.concert.domain.event.reservation.ReservationEvent

interface ReservationEventPublisher {
    fun publish(event: ReservationEvent)
}