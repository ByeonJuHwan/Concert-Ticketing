package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUseCase
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand
import org.springframework.stereotype.Service

@Service
class PaymentService (
    private val paymentRepository: PaymentRepository,
): PaymentUseCase {

    /**
     *
     */
    override fun pay(toCommand: PaymentCommand): PaymentResponse {
        TODO("Not yet implemented")
    }
}