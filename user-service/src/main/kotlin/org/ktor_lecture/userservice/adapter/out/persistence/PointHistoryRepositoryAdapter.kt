package org.ktor_lecture.userservice.adapter.out.persistence

import org.ktor_lecture.userservice.adapter.out.persistence.jpa.PointHistoryJpaRepository
import org.ktor_lecture.userservice.application.port.out.PointHistoryRepository
import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class PointHistoryRepositoryAdapter (
    private val pointHistoryJpaRepository: PointHistoryJpaRepository,
): PointHistoryRepository {
    override fun save(pointHistory: PointHistoryEntity): PointHistoryEntity {
        return pointHistoryJpaRepository.save(pointHistory)
    }

    override fun findById(pointHistoryId: Long): Optional<PointHistoryEntity> {
        return pointHistoryJpaRepository.findById(pointHistoryId)
    }
}