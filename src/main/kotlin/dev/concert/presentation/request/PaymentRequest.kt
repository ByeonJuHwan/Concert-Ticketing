package dev.concert.presentation.request

import dev.concert.application.payment.dto.PaymentDto

data class PaymentRequest(
    val reservationId : Long
)

fun PaymentRequest.toDto() = PaymentDto(
    reservationId = reservationId,
)