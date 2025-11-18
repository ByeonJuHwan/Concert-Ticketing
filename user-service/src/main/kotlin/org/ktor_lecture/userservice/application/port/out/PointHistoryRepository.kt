package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity

interface PointHistoryRepository {
    fun save(pointHistory: PointHistoryEntity)
}