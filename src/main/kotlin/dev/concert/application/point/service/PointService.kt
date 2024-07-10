package dev.concert.application.point.service

import dev.concert.application.point.dto.PointResponseDto
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.UserEntity

interface PointService {
    fun chargePoints(user: UserEntity, amount: Long): PointResponseDto
    fun getCurrentPoint(user: UserEntity): PointResponseDto
    fun checkPoint(user: UserEntity, price: Long) : PointEntity
    fun deductPoints(currentPoint : PointEntity, price: Long)
}