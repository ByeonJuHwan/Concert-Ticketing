package org.ktor_lecture.paymentservice.adapter.out.persistence

import org.ktor_lecture.paymentservice.adapter.out.persistence.jpa.PaymentUserJpaRepository
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.springframework.stereotype.Component

@Component
class PaymentRepositoryAdapter (
    private val paymentUserJpaRepository: PaymentUserJpaRepository,
): PaymentRepository {
    override fun createUser(user: PaymentUserEntity) {
        paymentUserJpaRepository.save(user)
    }
}