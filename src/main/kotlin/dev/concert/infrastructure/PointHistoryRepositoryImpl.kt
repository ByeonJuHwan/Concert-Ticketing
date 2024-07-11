package dev.concert.infrastructure

import dev.concert.domain.PointHistoryRepository
import dev.concert.domain.entity.PointHistoryEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.PointTransactionType
import dev.concert.infrastructure.jpa.PointHistoryJpaRepository
import org.springframework.stereotype.Repository

@Repository
class PointHistoryRepositoryImpl (
    private val pointHistoryJpaRepository: PointHistoryJpaRepository,
) : PointHistoryRepository {
    override fun saveHistory(history: PointHistoryEntity) {
        pointHistoryJpaRepository.save(history)
    }
}