package dev.concert.domain

import dev.concert.domain.entity.PointHistoryEntity

interface PointHistoryRepository {
    fun saveHistory(history: PointHistoryEntity)
}