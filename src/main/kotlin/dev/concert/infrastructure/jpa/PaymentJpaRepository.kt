package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long>{
}