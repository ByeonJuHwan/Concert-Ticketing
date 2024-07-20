package dev.concert.interfaces.presentation.response.payment

import dev.concert.application.payment.dto.PaymentResponseDto
import dev.concert.domain.entity.status.ReservationStatus

data class PaymentResponse(
    val reservationId : Long,
    val seatNo : Int,
    val status : ReservationStatus,
    val price : Long,
) {
    companion object {
        fun toResponse(payment: PaymentResponseDto): PaymentResponse {
            return PaymentResponse(
                reservationId = payment.reservationId,
                seatNo = payment.seatNo,
                status = payment.status,
                price = payment.price,
            )
        }
    }
}
