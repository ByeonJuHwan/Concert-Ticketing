package org.ktor_lecture.paymentservice.application.port.out

import org.ktor_lecture.paymentservice.domain.entity.IdempotencyEntity

interface IdempotencyRepository {
    fun findBySagaId(sagaId: String): IdempotencyEntity?
    fun save(entity: IdempotencyEntity): IdempotencyEntity
}