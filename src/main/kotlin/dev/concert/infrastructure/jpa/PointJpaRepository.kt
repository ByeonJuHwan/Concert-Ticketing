package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface PointJpaRepository : JpaRepository<PointEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByUser(user : UserEntity) : PointEntity?
}