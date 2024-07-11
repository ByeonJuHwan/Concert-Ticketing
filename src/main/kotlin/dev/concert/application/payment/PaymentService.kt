package dev.concert.application.payment

import dev.concert.domain.entity.ReservationEntity

interface PaymentService {
    fun createPayments(reservation: ReservationEntity)
    fun checkPayment(reservation: ReservationEntity)
}