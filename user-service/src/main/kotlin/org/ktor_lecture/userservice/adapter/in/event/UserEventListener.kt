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

    /**
     * [아웃박스 패턴]
     * BEFORE_COMMIT 으로 실행되어 어떤 이벤트가 발행되어야 하는지 저장한다
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleUserCreatedEvent(event: UserCreatedEvent) {
        createUserCreateOutBoxUseCase.recordUserCreatedOutBoxMsg(event)
    }

    /**
     * [아웃박스 패턴]
     * AFTER_COMMIT 시 카프카 이벤트를 발행한다
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishUseCreatedEvent(event: UserCreatedEvent) {
        sendUserCreatedEventUseCase.publishUseCreatedEvent(event)
    }
}