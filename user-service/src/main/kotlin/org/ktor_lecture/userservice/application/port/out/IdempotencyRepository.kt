package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.IdempotencyEntity

interface IdempotencyRepository {
    fun findBySagaId(sagaId: String): IdempotencyEntity?
    fun save(entity: IdempotencyEntity): IdempotencyEntity
}