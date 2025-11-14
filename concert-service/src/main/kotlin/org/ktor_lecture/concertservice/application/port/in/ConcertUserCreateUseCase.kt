package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.domain.event.UserCreatedEvent

interface ConcertUserCreateUseCase {

    fun createUser(event: UserCreatedEvent)
}