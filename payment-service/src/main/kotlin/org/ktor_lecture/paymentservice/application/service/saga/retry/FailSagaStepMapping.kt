package org.ktor_lecture.paymentservice.application.service.saga.retry

import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.springframework.stereotype.Component


interface SagaCompensationStrategy {
    fun compensate(saga: SagaEntity)
    fun supports(sagaType: String): Boolean
}

@Component
class SagaCompensationStrategyMapper(
    private val strategies: List<SagaCompensationStrategy>

){
    fun getStrategy(sagaType: String): SagaCompensationStrategy {
        return strategies.firstOrNull { it.supports(sagaType) }
            ?: throw IllegalArgumentException("보상 전락이 존재하지 않습니다")
    }
}



