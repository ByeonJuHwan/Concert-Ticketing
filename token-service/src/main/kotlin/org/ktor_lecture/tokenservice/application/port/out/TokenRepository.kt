package org.ktor_lecture.tokenservice.application.port.out

import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.entity.WaitingQueueEntity


interface TokenRepository {
    fun findTokenById(key: String): String?
    fun addWaitingQueue(user: QueueTokenUserEntity, currentTime: Double)
    fun createToken(key: String, token: String)
    fun getTokenExpireTime(key: String): Long?
    fun deleteAllActiveTokens()
    fun isTokenInActiveQueue(token: String): Boolean
    fun getRankInWaitingQueue(userJson: String): Long?
    fun findTopWaitingTokens(start: Long, end: Long): Set<String>?
    fun addActiveQueue(token: String)
    fun addActiveQueueEntity(user: QueueTokenUserEntity, token: String)
    fun removeWaitingQueueToken(userJson: String)
    fun removeWaitingQueueTokenEntity(entity: WaitingQueueEntity)
    fun deleteToken(key: String)
    fun saveToken(user: QueueTokenUserEntity, token: String)
    fun findTopWaitingQueueEntities(start: Long, end: Long): List<WaitingQueueEntity>
}