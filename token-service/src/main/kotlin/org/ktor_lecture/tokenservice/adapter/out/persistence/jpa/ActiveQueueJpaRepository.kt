package org.ktor_lecture.tokenservice.adapter.out.persistence.jpa

import org.ktor_lecture.tokenservice.domain.entity.ActiveTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ActiveQueueJpaRepository: JpaRepository<ActiveTokenEntity, Long> {
}