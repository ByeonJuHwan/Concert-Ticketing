package org.ktor_lecture.userservice.application.port.`in`

import org.ktor_lecture.userservice.domain.event.UserCreatedEvent

interface SendUserCreatedEventUseCase {
    fun publishUseCreatedEvent(event: UserCreatedEvent)
}