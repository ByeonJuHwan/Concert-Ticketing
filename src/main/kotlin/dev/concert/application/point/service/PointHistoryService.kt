package dev.concert.application.point.service

import dev.concert.domain.entity.UserEntity

interface PointHistoryService {
    fun saveChargePointHistory(user : UserEntity, amount: Long)
    fun saveUsePointHistory(user: UserEntity, amount: Long)
}