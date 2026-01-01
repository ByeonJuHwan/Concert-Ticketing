package org.ktor_lecture.paymentservice.adapter.out.persistence.jpa

import org.ktor_lecture.paymentservice.domain.entity.IdempotencyEntity
import org.springframework.data.jpa.repository.JpaRepository

interface IdempotencyJpaRepository : JpaRepository<IdempotencyEntity, Long> {
    fun findBySagaId(sagaId: String): IdempotencyEntity?
}
