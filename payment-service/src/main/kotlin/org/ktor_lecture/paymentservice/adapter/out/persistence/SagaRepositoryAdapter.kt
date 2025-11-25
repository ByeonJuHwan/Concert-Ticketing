package org.ktor_lecture.paymentservice.adapter.out.persistence

import org.ktor_lecture.paymentservice.adapter.out.persistence.jpa.SagaJpaRepository
import org.ktor_lecture.paymentservice.application.port.out.SagaRepository
import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class SagaRepositoryAdapter (
    private val sagaJpaRepository: SagaJpaRepository
): SagaRepository {
    override fun save(saga: SagaEntity): SagaEntity {
        return sagaJpaRepository.save(saga)
    }

    override fun findById(sagaId: Long): Optional<SagaEntity> {
        return sagaJpaRepository.findById(sagaId)
    }
}