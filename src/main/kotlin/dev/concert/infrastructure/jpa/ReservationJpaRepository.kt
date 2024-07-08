package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long> {
}