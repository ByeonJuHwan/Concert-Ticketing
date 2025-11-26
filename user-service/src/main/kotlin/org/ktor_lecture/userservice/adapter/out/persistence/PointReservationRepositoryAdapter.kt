package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.PointReservationJpaRepository
import org.ktor_lecture.userservice.application.port.out.PointReservationRepository
import org.ktor_lecture.userservice.domain.entity.PointReservationEntity
import org.springframework.stereotype.Component

@Component
class PointReservationRepositoryAdapter (
    private val pointReservationJpaRepository: PointReservationJpaRepository,
): PointReservationRepository {
    override fun findByRequestId(requestId: String): PointReservationEntity? {
        return pointReservationJpaRepository.findByRequestId(requestId)
    }

    override fun save(pointReservationEntity: PointReservationEntity) {
        pointReservationJpaRepository.save(pointReservationEntity)
    }
}