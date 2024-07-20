package dev.concert.domain.repository

import dev.concert.domain.entity.PointHistoryEntity

interface PointHistoryRepository {
    fun saveHistory(history: PointHistoryEntity)
}