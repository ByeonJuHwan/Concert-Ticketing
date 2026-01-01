package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.IdempotencyJpaRepository
import org.ktor_lecture.concertservice.application.port.out.IdempotencyRepository
import org.ktor_lecture.concertservice.domain.entity.IdempotencyEntity
import org.springframework.stereotype.Component

@Component
class IdempotencyRepositoryAdapter (
    private val idempotencyJpaRepository: IdempotencyJpaRepository,
): IdempotencyRepository {
    override fun findBySagaId(sagaId: String): IdempotencyEntity? {
        return idempotencyJpaRepository.findBySagaId(sagaId)
    }

    override fun save(entity: IdempotencyEntity): IdempotencyEntity {
        return idempotencyJpaRepository.save(entity)
    }
}