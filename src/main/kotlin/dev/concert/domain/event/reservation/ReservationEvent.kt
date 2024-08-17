package dev.concert.domain.event.reservation

import dev.concert.domain.entity.outbox.ReservationEventOutBox

interface ReservationEvent {
    fun toKafkaMessage() : String
    fun toEntity() : ReservationEventOutBox
}

data class ReservationSuccessEvent(val reservationId: Long) : ReservationEvent {
    override fun toKafkaMessage() : String{
        return reservationId.toString()
    }

    override fun toEntity(): ReservationEventOutBox {
        return ReservationEventOutBox(reservationId)
    }
}