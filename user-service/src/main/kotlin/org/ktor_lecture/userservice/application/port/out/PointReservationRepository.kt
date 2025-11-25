package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.PointReservationEntity

interface PointReservationRepository {
    fun findByRequestId(requestId: String): PointReservationEntity?
    fun save(pointReservationEntity: org.ktor_lecture.userservice.domain.entity.PointReservationEntity)
}