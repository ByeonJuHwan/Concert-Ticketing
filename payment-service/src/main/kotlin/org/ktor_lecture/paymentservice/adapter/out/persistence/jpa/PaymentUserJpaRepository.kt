package org.ktor_lecture.paymentservice.adapter.out.persistence.jpa

import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentUserJpaRepository: JpaRepository<PaymentUserEntity, Long> {
}