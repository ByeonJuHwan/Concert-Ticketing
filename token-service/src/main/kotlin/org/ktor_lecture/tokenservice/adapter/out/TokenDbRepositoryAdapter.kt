package org.ktor_lecture.tokenservice.adapter.out

import org.ktor_lecture.tokenservice.adapter.out.persistence.jpa.ActiveQueueJpaRepository
import org.ktor_lecture.tokenservice.adapter.out.persistence.jpa.TokenJpaRepository
import org.ktor_lecture.tokenservice.adapter.out.persistence.jpa.WaitingQueueJpaRepository
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.domain.entity.ActiveTokenEntity
import org.ktor_lecture.tokenservice.domain.entity.QueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.entity.TokenEntity
import org.ktor_lecture.tokenservice.domain.entity.WaitingQueueEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("DB")
class TokenDbRepositoryAdapter (
    private val activeQueueJpaRepository: ActiveQueueJpaRepository,
    private val waitingQueueJpaRepository: WaitingQueueJpaRepository,
    private val tokenJpaRepository: TokenJpaRepository,
): TokenRepository {
    override fun findTokenById(key: String): String? {
        return tokenJpaRepository.findByUserId(key.toLong())
    }

    override fun addWaitingQueue(user: QueueTokenUserEntity, currentTime: Double) {
        val waitingQueueEntity = WaitingQueueEntity(
            queueTokenUser = user,
            score = currentTime,
        )

        waitingQueueJpaRepository.save(waitingQueueEntity)
    }

    override fun createToken(key: String, token: String) {
    }

    override fun getTokenExpireTime(key: String): Long? {
        TODO("Not yet implemented")
    }

    override fun deleteAllActiveTokens() {
        TODO("Not yet implemented")
    }

    override fun isTokenInActiveQueue(token: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRankInWaitingQueue(userJson: String): Long? {
        TODO("Not yet implemented")
    }

    override fun findTopWaitingTokens(start: Long, end: Long): Set<String>? {
        return null
    }

    override fun addActiveQueue(token: String) {
        TODO("Not yet implemented")
    }

    override fun addActiveQueueEntity(user: QueueTokenUserEntity, token: String) {
        val activeQueueToken = ActiveTokenEntity(
            token = token,
            queueTokenUser = user,
        )
        activeQueueJpaRepository.save(activeQueueToken)
    }

    override fun removeWaitingQueueToken(userJson: String) {
        TODO("Not yet implemented")
    }

    override fun removeWaitingQueueTokenEntity(entity: WaitingQueueEntity) {
        waitingQueueJpaRepository.delete(entity)
    }

    override fun deleteToken(key: String) {
        TODO("Not yet implemented")
    }

    override fun saveToken(user: QueueTokenUserEntity, token: String) {
        val tokenEntity = TokenEntity(
            token = token,
            queueTokenUser = user,
        )

        tokenJpaRepository.save(tokenEntity)
    }

    override fun findTopWaitingQueueEntities(start: Long, end: Long): List<WaitingQueueEntity> {
        return waitingQueueJpaRepository.findTopWaitingTokens(start, end)
    }
}