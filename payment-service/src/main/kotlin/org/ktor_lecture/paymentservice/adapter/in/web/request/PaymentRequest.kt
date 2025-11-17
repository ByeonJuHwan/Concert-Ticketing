package org.ktor_lecture.paymentservice.adapter.`in`.web.request

import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand

data class PaymentRequest(
    val reservationId : Long
) {
    fun toCommand() = PaymentCommand(
        reservationId = reservationId
    )
}
