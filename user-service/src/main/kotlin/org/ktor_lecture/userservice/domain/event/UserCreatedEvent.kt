package org.ktor_lecture.userservice.domain.event

import kotlinx.serialization.SerialName
import java.util.UUID
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
@SerialName("UserCreated")
data class UserCreatedEvent(
    val userId: String,
    val userName: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredAt: String = LocalDateTime.now().toString(),
): DomainEvent()
