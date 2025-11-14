package org.ktor_lecture.userservice.adapter.out.persistence.jpa

import jakarta.persistence.LockModeType
import org.ktor_lecture.userservice.domain.entity.PointEntity
import org.ktor_lecture.userservice.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface PointJpaRepository : JpaRepository<PointEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByUser(user: UserEntity): PointEntity?
}