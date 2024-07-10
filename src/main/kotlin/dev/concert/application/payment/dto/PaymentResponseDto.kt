package dev.concert.application.payment.dto

import dev.concert.domain.entity.status.ReservationStatus

data class PaymentResponseDto(
    val reservationId : Long,
    val seatNo : Int,
    val status : ReservationStatus,
    val price : Long,
)
