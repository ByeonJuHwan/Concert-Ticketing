package org.ktor_lecture.paymentservice.application.port.`in`.grpc

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand

interface PaymentGrpcUseCase {
    suspend fun pay(command: PaymentCommand): PaymentResponse
}