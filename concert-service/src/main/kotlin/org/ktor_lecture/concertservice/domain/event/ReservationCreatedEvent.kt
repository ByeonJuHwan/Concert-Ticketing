package org.ktor_lecture.concertservice.domain.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
@SerialName("ReservationCreated")
data class ReservationCreatedEvent(
    val reservationId: Long,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: String = LocalDateTime.now().toString(),
) : DomainEvent()
