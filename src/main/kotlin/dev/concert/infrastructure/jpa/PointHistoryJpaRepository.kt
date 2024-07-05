package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PointHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PointHistoryJpaRepository : JpaRepository<PointHistoryEntity, Long>{
}