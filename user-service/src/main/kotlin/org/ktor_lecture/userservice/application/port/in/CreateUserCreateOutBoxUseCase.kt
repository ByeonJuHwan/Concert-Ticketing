package org.ktor_lecture.userservice.application.port.`in`

import org.ktor_lecture.userservice.domain.event.UserCreatedEvent

interface CreateUserCreateOutBoxUseCase {
    fun recordUserCreatedOutBoxMsg(event: UserCreatedEvent)
}