package org.ktor_lecture.userservice.adapter.out.persistence.jpa

import org.ktor_lecture.userservice.domain.entity.PointReservationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PointReservationJpaRepository: JpaRepository<PointReservationEntity, Long> {
    fun findByRequestId(requestId: String): PointReservationEntity?
}