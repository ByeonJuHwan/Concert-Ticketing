package dev.concert.application.point.service

import dev.concert.domain.entity.UserEntity

interface PointHistoryService {
    fun savePointHistory(user : UserEntity, amount: Long)
}