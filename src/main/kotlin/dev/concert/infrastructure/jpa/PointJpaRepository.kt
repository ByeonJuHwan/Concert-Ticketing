package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PointJpaRepository : JpaRepository<PointEntity, Long> {
    @Query("select p from PointEntity p join fetch p.user where p.user = :user")
    fun findByUser(user : UserEntity) : PointEntity?
}