package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import org.ktor_lecture.concertservice.domain.entity.IdempotencyEntity
import org.springframework.data.jpa.repository.JpaRepository

interface IdempotencyJpaRepository : JpaRepository<IdempotencyEntity, Long> {
    fun findBySagaId(sagaId: String): IdempotencyEntity?
}
