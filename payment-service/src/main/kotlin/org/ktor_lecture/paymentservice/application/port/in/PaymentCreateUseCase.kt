package org.ktor_lecture.paymentservice.application.port.`in`

import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand

interface PaymentCreateUseCase {

    fun save(command: PaymentCreateCommand): String
}