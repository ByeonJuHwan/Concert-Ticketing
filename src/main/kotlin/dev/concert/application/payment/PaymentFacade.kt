package dev.concert.application.payment

import dev.concert.application.payment.dto.PaymentDto
import dev.concert.application.payment.dto.PaymentResponseDto
import dev.concert.domain.service.payment.PaymentService
import dev.concert.domain.service.token.TokenService
import org.springframework.stereotype.Component

@Component
class PaymentFacade(
    private val tokenService: TokenService,
    private val paymentService: PaymentService,
) {
    fun pay(request: PaymentDto) : PaymentResponseDto {
        val payment = paymentService.processReservationPayment(request.reservationId)
        tokenService.deleteToken(payment.reservation.user)
        return PaymentResponseDto( 
            reservationId = payment.reservation.id,
            seatNo = payment.reservation.seat.seatNo,
            status = payment.reservation.status,
            price = payment.reservation.seat.price,
        ) 
    } 
}
