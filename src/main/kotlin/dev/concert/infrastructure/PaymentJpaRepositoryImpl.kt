package dev.concert.infrastructure

import dev.concert.domain.PaymentRepository
import dev.concert.domain.entity.PaymentEntity
import dev.concert.infrastructure.jpa.PaymentJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PaymentJpaRepositoryImpl (
    private val paymentRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(paymentEntity: PaymentEntity): PaymentEntity {
        return paymentRepository.save(paymentEntity)
    }
}