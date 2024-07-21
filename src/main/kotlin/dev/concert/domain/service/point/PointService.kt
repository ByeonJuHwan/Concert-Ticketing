package dev.concert.domain.service.point

import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity

interface PointService {
    fun chargePoints(user: UserEntity, amount: Long): PointEntity
    fun getCurrentPoint(user: UserEntity): PointEntity
    fun checkPoint(user: UserEntity, price: Long) : PointEntity
    fun deductPoints(currentPoint : PointEntity, price: Long)
}