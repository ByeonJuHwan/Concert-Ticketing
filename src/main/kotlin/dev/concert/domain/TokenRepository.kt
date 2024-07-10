package dev.concert.domain

import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity

interface TokenRepository {
    fun saveToken(tokenEntity: QueueTokenEntity): QueueTokenEntity
    fun findLastQueueOrder(): Int
    fun findByToken(token: String): QueueTokenEntity?
    fun deleteToken(user: UserEntity)
}