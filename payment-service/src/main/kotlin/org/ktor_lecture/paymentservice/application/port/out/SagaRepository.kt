package org.ktor_lecture.paymentservice.application.port.out

import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import java.util.Optional

interface SagaRepository {
    fun save(saga: SagaEntity): SagaEntity
    fun findById(sagaId: Long): Optional<SagaEntity>
}