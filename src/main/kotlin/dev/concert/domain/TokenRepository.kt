package dev.concert.domain

import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity

interface TokenRepository {
    fun saveToken(tokenEntity: QueueTokenEntity): QueueTokenEntity
    fun findFirstQueueOrderId(): Long
    fun findByToken(token: String): QueueTokenEntity?
    fun deleteToken(user: UserEntity)
    fun findWaitingAndActiveTokens(): List<QueueTokenEntity>
    fun deleteByToken(tokenEntity: QueueTokenEntity)
    fun deleteExpiredTokens()
}