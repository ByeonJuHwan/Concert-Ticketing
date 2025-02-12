package dev.concert.domain.repository

import dev.concert.domain.entity.PaymentEntity
import dev.concert.domain.entity.ReservationEntity

interface PaymentRepository {
    fun save(paymentEntity: PaymentEntity) : PaymentEntity
    fun existsByReservation(reservation: ReservationEntity): Boolean
}