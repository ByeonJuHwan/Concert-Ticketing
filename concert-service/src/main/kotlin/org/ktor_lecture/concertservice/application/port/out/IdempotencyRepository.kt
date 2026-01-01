package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.IdempotencyEntity

interface IdempotencyRepository {
    fun findBySagaId(sagaId: String): IdempotencyEntity?
    fun save(entity: IdempotencyEntity): IdempotencyEntity
}