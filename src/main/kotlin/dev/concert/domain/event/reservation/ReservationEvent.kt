package dev.concert.domain.event.reservation

interface ReservationEvent {
    fun toKafkaMessage() : String
}

data class ReservationSuccessEvent(val reservationId: Long) : ReservationEvent {
    override fun toKafkaMessage() : String{
        return reservationId.toString()
    }
}