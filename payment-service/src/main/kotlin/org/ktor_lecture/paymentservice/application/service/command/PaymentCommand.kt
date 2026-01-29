package org.ktor_lecture.paymentservice.application.service.command

data class PaymentCommand (
    val reservationId: Long,
)

data class PaymentCreateCommand(
    val price: Long,
    val reservationId: Long,
    val userId: Long,
)