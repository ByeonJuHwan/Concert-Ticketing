package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import org.springframework.data.jpa.repository.JpaRepository

interface TokenJpaRepository : JpaRepository<QueueTokenEntity, Long> {

    fun findTopByStatusOrderByQueueOrderDesc(status: QueueTokenStatus): QueueTokenEntity?
    fun findByToken(token : String): QueueTokenEntity?
}