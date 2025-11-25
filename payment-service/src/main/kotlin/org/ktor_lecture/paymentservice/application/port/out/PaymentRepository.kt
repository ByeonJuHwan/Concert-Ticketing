package org.ktor_lecture.paymentservice.application.port.out

import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import java.util.Optional

interface PaymentRepository {
    fun createUser(user: PaymentUserEntity)
    fun save(payment: PaymentEntity): PaymentEntity
    fun findById(paymentId: Long): Optional<PaymentEntity>
}