package dev.concert.domain.repository

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity

interface PointRepository {
    fun findByUser(user : UserEntity) : PointEntity?
    fun save(point : PointEntity) : PointEntity
    fun deleteAll()
}