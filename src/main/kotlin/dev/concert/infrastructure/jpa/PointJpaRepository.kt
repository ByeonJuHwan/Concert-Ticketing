package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PointJpaRepository : JpaRepository<PointEntity, Long> {
    fun findByUser(user : UserEntity) : PointEntity?
}