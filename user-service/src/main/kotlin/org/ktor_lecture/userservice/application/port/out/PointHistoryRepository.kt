package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity
import java.util.Optional

interface PointHistoryRepository {
    fun save(pointHistory: PointHistoryEntity): PointHistoryEntity
    fun findById(pointHistoryId: Long): Optional<PointHistoryEntity>
}