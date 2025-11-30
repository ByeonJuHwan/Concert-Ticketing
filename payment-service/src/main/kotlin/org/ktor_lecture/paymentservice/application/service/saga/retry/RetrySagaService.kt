package org.ktor_lecture.paymentservice.application.service.saga.retry

import org.ktor_lecture.paymentservice.application.port.`in`.saga.RetryFailSagaUseCase
import org.ktor_lecture.paymentservice.application.port.out.SagaRepository
import org.springframework.stereotype.Service

@Service
class RetrySagaService (
    private val sagaRepository: SagaRepository,
    private val sagaCompensationStrategyMapper: SagaCompensationStrategyMapper,
): RetryFailSagaUseCase {


    override fun retryFailSagas() {
        val failedSagaList = sagaRepository.getFailedSagas()

        for (saga in failedSagaList) {
            val strategy = sagaCompensationStrategyMapper.getStrategy(saga.sagaType)
            strategy.compensate(saga)
        }
    }
}