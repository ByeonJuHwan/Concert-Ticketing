package org.ktor_lecture.paymentservice.application.port.`in`.http

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand

interface PaymentUseCase {
    fun pay(command: PaymentCommand): PaymentResponse
}