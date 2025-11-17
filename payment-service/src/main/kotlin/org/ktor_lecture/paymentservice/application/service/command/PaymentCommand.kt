package org.ktor_lecture.paymentservice.application.service.command

data class PaymentCommand (
    val reservationId: Long,
)