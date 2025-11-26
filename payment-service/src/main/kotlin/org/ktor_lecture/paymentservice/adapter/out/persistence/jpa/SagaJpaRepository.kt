package org.ktor_lecture.paymentservice.adapter.out.persistence.jpa

import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SagaJpaRepository: JpaRepository<SagaEntity, Long> {
}