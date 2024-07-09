package dev.concert.application.point.service

import dev.concert.domain.PointHistoryRepository
import dev.concert.domain.entity.UserEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointHistoryServiceImpl (
    private val pointHistoryRepository: PointHistoryRepository,
) : PointHistoryService {
    @Transactional
    override fun savePointHistory(user : UserEntity, amount: Long) {
        return pointHistoryRepository.saveHistory(user, amount)
    }
}