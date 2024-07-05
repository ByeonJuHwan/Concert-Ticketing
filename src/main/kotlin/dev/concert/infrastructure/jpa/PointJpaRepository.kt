package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface PointJpaRepository : JpaRepository<PointEntity, Long> {
    fun findByUser(user : UserEntity) : PointEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointEntity p where p.user = :user")
    fun findByUserWithLock(user : UserEntity) : PointEntity?
}