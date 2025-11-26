package org.ktor_lecture.paymentservice.adapter.out.persistence.jpa

import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository: JpaRepository<PaymentEntity, Long> {
}