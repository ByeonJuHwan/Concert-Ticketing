package dev.concert.infrastructure

import dev.concert.domain.repository.TokenRepository
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import dev.concert.infrastructure.jpa.TokenJpaRepository
import org.springframework.stereotype.Repository

@Repository
class TokenRepositoryImpl(
    private val tokenJpaRepository: TokenJpaRepository
) : TokenRepository {

    override fun saveToken(tokenEntity: QueueTokenEntity): QueueTokenEntity {
        return tokenJpaRepository.save(tokenEntity)
    }

    override fun findFirstQueueOrderId(): Long {
        tokenJpaRepository.findFirstIdInQueueOrderStatusWaiting(QueueTokenStatus.WAITING)?.let{
            return it.id
        }
        return 0
    }

    override fun findByToken(token: String): QueueTokenEntity? {
        return tokenJpaRepository.findByToken(token)
    }

    override fun deleteByUser(user: UserEntity) {
        tokenJpaRepository.deleteByUser(user)
    }

    override fun findWaitingAndActiveTokens(): List<QueueTokenEntity> {
        return tokenJpaRepository.findAvailableTokens()
    }

    override fun deleteByToken(tokenEntity: QueueTokenEntity) {
        tokenJpaRepository.delete(tokenEntity)
    }

    override fun deleteExpiredTokens() {
        tokenJpaRepository.deleteExpiredTokens()
    }

    override fun findByUser(user: UserEntity): QueueTokenEntity? {
        return tokenJpaRepository.findByUser(user)
    }
}