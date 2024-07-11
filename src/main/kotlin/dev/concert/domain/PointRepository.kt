package dev.concert.domain

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity

interface PointRepository {
    fun findByUser(user : UserEntity) : PointEntity?
    fun save(point : PointEntity) : PointEntity
    fun findByUserWithLock(user : UserEntity) : PointEntity?
    fun deleteAll()
}