package org.ktor_lecture.userservice.adapter.`in`.event

import org.ktor_lecture.userservice.application.port.`in`.CreateUserCreateOutBoxUseCase
import org.ktor_lecture.userservice.application.port.`in`.SendUserCreatedEventUseCase
import org.ktor_lecture.userservice.domain.event.UserCreatedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener (
    private val createUserCreateOutBoxUseCase: CreateUserCreateOutBoxUseCase,
    private val sendUserCreatedEventUseCase: SendUserCreatedEventUseCase,
) {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleUserCreatedEvent(event: UserCreatedEvent) {
        createUserCreateOutBoxUseCase.recordUserCreatedOutBoxMsg(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishUseCreatedEvent(event: UserCreatedEvent) {
        sendUserCreatedEventUseCase.publishUseCreatedEvent(event)
    }
}