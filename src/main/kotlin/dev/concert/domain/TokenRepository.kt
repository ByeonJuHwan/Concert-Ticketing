package dev.concert.domain

import dev.concert.domain.entity.QueueTokenEntity

interface TokenRepository {
    fun saveToken(tokenEntity: QueueTokenEntity): QueueTokenEntity
    fun findLastQueueOrder(): Int
}