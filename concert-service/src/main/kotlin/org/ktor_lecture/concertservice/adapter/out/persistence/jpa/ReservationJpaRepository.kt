package org.ktor_lecture.concertservice.adapter.out.persistence.jpa

import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationJpaRepository: JpaRepository<ReservationEntity, Long> {
}