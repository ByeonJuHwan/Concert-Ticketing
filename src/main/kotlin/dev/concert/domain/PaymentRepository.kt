package dev.concert.domain

import dev.concert.domain.entity.PaymentEntity

interface PaymentRepository {
    fun save(paymentEntity: PaymentEntity) : PaymentEntity
}