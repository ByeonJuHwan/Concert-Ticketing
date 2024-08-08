package dev.concert.domain.event.publisher

import dev.concert.domain.event.ReservationSuccessEvent

interface ReservationEventPublisher {
    fun publish(event : ReservationSuccessEvent)
}