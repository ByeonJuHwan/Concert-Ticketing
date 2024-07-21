package dev.concert.domain.service.payment

import dev.concert.domain.entity.PaymentEntity

interface PaymentService {
    fun processReservationPayment(reservationId: Long) : PaymentEntity
}