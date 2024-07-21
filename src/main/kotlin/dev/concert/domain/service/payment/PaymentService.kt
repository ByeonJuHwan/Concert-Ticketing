package dev.concert.domain.service.payment

import dev.concert.domain.entity.ReservationEntity

interface PaymentService {
    fun createPayments(reservation: ReservationEntity)
}