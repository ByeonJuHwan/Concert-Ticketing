package dev.concert.infrastructure.jpa

import dev.concert.domain.entity.QQueueTokenEntity.queueTokenEntity
import dev.concert.domain.entity.QueueTokenEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.QueueTokenStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface TokenJpaRepository : JpaRepository<QueueTokenEntity, Long>, TokenJpaRepositoryCustom {
    fun findByToken(token : String): QueueTokenEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from QueueTokenEntity q where q.user = :user")
    fun deleteByUser(user: UserEntity)
}

interface TokenJpaRepositoryCustom {
    fun findAvailableTokens(): List<QueueTokenEntity>
    fun findFirstIdInQueueOrderStatusWaiting(waiting: QueueTokenStatus): QueueTokenEntity?
    fun deleteExpiredTokens()
}

class TokenJpaRepositoryImpl : TokenJpaRepositoryCustom, QuerydslRepositorySupport(QueueTokenEntity::class.java) {
    override fun findAvailableTokens(): List<QueueTokenEntity> {
        return from(queueTokenEntity)
            .where(queueTokenEntity.status.eq(QueueTokenStatus.WAITING).or(queueTokenEntity.status.eq(QueueTokenStatus.ACTIVE)))
            .limit(30)
            .fetch()
    }

    override fun findFirstIdInQueueOrderStatusWaiting(waiting: QueueTokenStatus): QueueTokenEntity? {
        return from(queueTokenEntity)
            .where(queueTokenEntity.status.eq(waiting))
            .fetchFirst()
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    override fun deleteExpiredTokens() {
        delete(queueTokenEntity)
            .where(queueTokenEntity.status.eq(QueueTokenStatus.EXPIRED))
            .execute()
    }
}