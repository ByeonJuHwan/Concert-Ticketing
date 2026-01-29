package org.ktor_lecture.paymentservice.adapter.out.persistence

import org.ktor_lecture.paymentservice.adapter.out.persistence.jpa.PaymentJpaRepository
import org.ktor_lecture.paymentservice.adapter.out.persistence.jpa.PaymentUserJpaRepository
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class PaymentRepositoryAdapter (
    private val paymentUserJpaRepository: PaymentUserJpaRepository,
    private val paymentJpaRepository: PaymentJpaRepository,
): PaymentRepository {
    override fun createUser(user: PaymentUserEntity) {
        paymentUserJpaRepository.save(user)
    }

    override fun save(payment: PaymentEntity): PaymentEntity {
        return paymentJpaRepository.save(payment)
    }

    override fun findById(paymentId: Long): Optional<PaymentEntity> {
        return paymentJpaRepository.findById(paymentId)
    }

    override fun findByUserId(userId: Long): List<PaymentEntity> {
        return paymentJpaRepository.findByUserId(userId)
    }
}