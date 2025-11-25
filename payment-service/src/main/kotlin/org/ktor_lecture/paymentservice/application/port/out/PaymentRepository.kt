package org.ktor_lecture.paymentservice.application.port.out

import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity

interface PaymentRepository {
    fun createUser(user: PaymentUserEntity)
    fun save(payment: org.ktor_lecture.paymentservice.domain.entity.PaymentEntity)
}