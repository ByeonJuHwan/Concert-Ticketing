package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.PointHistoryJpaRepository
import org.ktor_lecture.userservice.application.port.out.PointHistoryRepository
import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity
import org.springframework.stereotype.Component

@Component
class PointHistoryRepositoryAdapter (
    private val pointHistoryJpaRepository: PointHistoryJpaRepository,
): PointHistoryRepository {
    override fun save(pointHistory: PointHistoryEntity) {
        pointHistoryJpaRepository.save(pointHistory)
    }
}