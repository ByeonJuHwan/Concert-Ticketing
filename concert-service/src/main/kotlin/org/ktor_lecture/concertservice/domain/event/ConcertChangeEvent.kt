package org.ktor_lecture.concertservice.domain.event

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ConcertOptionChangeEvent(
    val concertId: Long,
    val concertOptionId: Long,
    val availableSeats: Int,
    val concertDate: String,
    val concertTime: String,
    val concertVenue: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: String = LocalDateTime.now().toString(),
) : DomainEvent() {

}