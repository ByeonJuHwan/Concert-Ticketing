package org.ktor_lecture.tokenservice.adapter.out.persistence.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.tokenservice.domain.entity.QQueueTokenUserEntity
import org.ktor_lecture.tokenservice.domain.entity.QWaitingQueueEntity
import org.ktor_lecture.tokenservice.domain.entity.WaitingQueueEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WaitingQueueJpaRepository: JpaRepository<WaitingQueueEntity, Long>, WaitingQueueJpaCustomRepository

interface WaitingQueueJpaCustomRepository {
    fun findTopWaitingTokens(start: Long, end: Long): List<WaitingQueueEntity>
}

class WaitingQueueJpaCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
): WaitingQueueJpaCustomRepository {
    override fun findTopWaitingTokens(start: Long, end: Long): List<WaitingQueueEntity> {
        val waitingQueueEntity = QWaitingQueueEntity.waitingQueueEntity
        val queueTokenUser = QQueueTokenUserEntity.queueTokenUserEntity

        val limit = end - start

        return queryFactory
            .selectFrom(waitingQueueEntity)
            .innerJoin(waitingQueueEntity.queueTokenUser, queueTokenUser).fetchJoin()
            .orderBy(waitingQueueEntity.id.asc())
            .limit(limit)
            .fetch()
    }
}