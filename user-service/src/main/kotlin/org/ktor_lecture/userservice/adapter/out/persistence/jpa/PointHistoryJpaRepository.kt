package org.ktor_lecture.userservice.adapter.out.persistence.jpa

import org.ktor_lecture.userservice.domain.entity.PointHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PointHistoryJpaRepository: JpaRepository<PointHistoryEntity, Long> {
}