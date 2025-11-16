package org.ktor_lecture.tokenservice.application.port.`in`

import org.ktor_lecture.tokenservice.domain.event.UserCreatedEvent

interface QueueTokenUserCreateUseCase {
    fun createTokenUser(event: UserCreatedEvent)
}