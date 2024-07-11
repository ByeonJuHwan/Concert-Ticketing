package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QQueueTokenEntity.queueTokenEntity
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface TokenJpaRepository : JpaRepository<QueueTokenEntity, Long>, TokenJpaRepositoryCustom {

    fun findByToken(token : String): QueueTokenEntity?
    fun deleteByUser(user: UserEntity)
}

interface TokenJpaRepositoryCustom {
    fun findAvaliableTokens(): List<QueueTokenEntity>
    fun findFirstIdInQueueOrderStatusWating(waiting: QueueTokenStatus): QueueTokenEntity?
}

class TokenJpaRepositoryImpl : TokenJpaRepositoryCustom, QuerydslRepositorySupport(QueueTokenEntity::class.java) {
    override fun findAvaliableTokens(): List<QueueTokenEntity> {
        return from(queueTokenEntity)
            .where(queueTokenEntity.status.eq(QueueTokenStatus.WAITING).or(queueTokenEntity.status.eq(QueueTokenStatus.ACTIVE)))
            .limit(30)
            .fetch()
    }

    override fun findFirstIdInQueueOrderStatusWating(waiting: QueueTokenStatus): QueueTokenEntity? {
        return from(queueTokenEntity)
            .where(queueTokenEntity.status.eq(waiting))
            .fetchFirst()
    }
}