package org.ktor_lecture.paymentservice.application.port.`in`

import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity

interface PaymentCreateUseCase {

    fun save(command: PaymentCreateCommand): PaymentEntity
}