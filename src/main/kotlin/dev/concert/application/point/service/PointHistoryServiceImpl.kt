package dev.concert.application.point.service

import dev.concert.domain.repository.PointHistoryRepository
import dev.concert.domain.entity.PointHistoryEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.PointTransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointHistoryServiceImpl (
    private val pointHistoryRepository: PointHistoryRepository,
) : PointHistoryService {
    @Transactional
    override fun saveChargePointHistory(user: UserEntity, amount: Long) {
        val history = createHistoryEntity(user, amount, PointTransactionType.CHARGE)
        return pointHistoryRepository.saveHistory(history)
    }
    @Transactional
    override fun saveUsePointHistory(user: UserEntity, amount: Long) {
        val history = createHistoryEntity(user, amount, PointTransactionType.USE)
        return pointHistoryRepository.saveHistory(history)
    }

    private fun createHistoryEntity(
        user: UserEntity,
        amount: Long,
        type: PointTransactionType
    ): PointHistoryEntity {
        val history = PointHistoryEntity(
            user = user,
            amount = amount,
            type = type,
        )
        return history
    }
}