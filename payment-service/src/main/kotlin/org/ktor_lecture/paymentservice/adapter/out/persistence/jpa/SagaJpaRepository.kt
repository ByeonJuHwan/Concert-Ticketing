package org.ktor_lecture.paymentservice.adapter.out.persistence.jpa

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.ktor_lecture.paymentservice.domain.entity.QSagaEntity
import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.ktor_lecture.paymentservice.domain.entity.SagaStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface SagaJpaRepository: JpaRepository<SagaEntity, Long>, SagaJpaRepositoryCustom {
}

interface SagaJpaRepositoryCustom {
    fun getFailedSagas(): List<SagaEntity>
}

class SagaJpaRepositoryCustomImpl (
    private val queryFactory: JPAQueryFactory,
): SagaJpaRepositoryCustom {

    val saga: QSagaEntity = QSagaEntity.sagaEntity

    override fun getFailedSagas(): List<SagaEntity> {

        return queryFactory
            .selectFrom(saga)
            .where(
                isRetryTarget()
            )
            .fetch()
    }

    private fun isRetryTarget(): BooleanExpression {
        return isFailedWithFailurePoint()
            .or(isCompensatingTimeout())
            .or(isInProgressTimeout())
    }

    /**
     * 상태가 Fail 인경우
     */
    private fun isFailedWithFailurePoint(): BooleanExpression {
        return saga.status.eq(SagaStatus.FAILED)
    }

    /**
     * 실패 지점이 존재하고, 상태가 COMPENSATING이지만 10분 이상 완료되지 않은 경우
     */
    private fun isCompensatingTimeout(): BooleanExpression{
        val tenMinutesAgo = LocalDateTime.now().minusMinutes(10)

        return saga.status.eq(SagaStatus.COMPENSATING)
            .and(saga.createdAt.before(tenMinutesAgo))
    }

    /**
     * 상태가 IN_PROGRESS이지만 5분 이상 진행 중인 경우
     */
    private fun isInProgressTimeout(): BooleanExpression{
        val fiveMinutesAgo = LocalDateTime.now().minusMinutes(5)

        return saga.status.eq(SagaStatus.IN_PROGRESS)
            .and(saga.updatedAt.before(fiveMinutesAgo))
    }

}