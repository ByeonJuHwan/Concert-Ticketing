package org.ktor_lecture.tokenservice.adapter.out.persistence.jpa

import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface QueueTokenJpaRepository: JpaRepository<QueueTokenUserEntity, Long> {
}