package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.adapter.`in`.consumer.event.UserCreatedEvent

interface ConcertUserCreateUseCase {

    fun createUser(event: UserCreatedEvent)
}