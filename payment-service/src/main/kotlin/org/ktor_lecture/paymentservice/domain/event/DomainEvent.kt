package org.ktor_lecture.paymentservice.domain.event

import kotlinx.serialization.Serializable

@Serializable
sealed class DomainEvent {
    abstract val eventId: String
    abstract val occurredAt: String
}
