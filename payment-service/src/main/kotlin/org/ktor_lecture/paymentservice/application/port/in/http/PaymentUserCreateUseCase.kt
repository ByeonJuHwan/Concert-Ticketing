package org.ktor_lecture.paymentservice.application.port.`in`.http

import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent

interface PaymentUserCreateUseCase {
    fun createUser(event: UserCreatedEvent)
    fun cancelPayment(paymentId: Long)
}