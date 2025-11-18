package org.ktor_lecture.paymentservice.application.port.`in`

import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent

interface PaymentUserCreateUseCase {
    fun createUser(event: UserCreatedEvent)
}