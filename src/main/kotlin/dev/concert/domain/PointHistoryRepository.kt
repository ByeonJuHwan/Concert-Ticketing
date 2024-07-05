package dev.concert.domain

import dev.concert.domain.entity.UserEntity

interface PointHistoryRepository {
    fun saveHistory(user: UserEntity, amount: Long)
}