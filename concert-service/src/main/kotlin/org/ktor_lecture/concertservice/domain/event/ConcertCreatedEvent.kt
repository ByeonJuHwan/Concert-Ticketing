package org.ktor_lecture.concertservice.domain.event

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ConcertCreatedEvent(
    val id: String,
    val concertName: String,
    val singer: String,
    val startDate: String,
    val endDate: String,
    val reserveStartDate: String,
    val reserveEndDate: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: String = LocalDateTime.now().toString(),
): DomainEvent()