package org.ktor_lecture.paymentservice.application.service.saga.retry

import org.ktor_lecture.paymentservice.application.port.`in`.saga.RetryFailSagaUseCase
import org.ktor_lecture.paymentservice.application.port.out.SagaRepository
import org.springframework.stereotype.Service

@Service
class RetrySagaService (
    private val sagaRepository: SagaRepository,
    private val sagaCompensationStrategyMapper: SagaCompensationStrategyMapper,
    private val sagaGrpcCompensationStrategyMapper: SagaGrpcCompensationStrategyMapper
): RetryFailSagaUseCase {


    /**
     * http saga 재시도
     */
    override fun retryFailSagas() {
        val failedSagaList = sagaRepository.getFailedSagas()

        for (saga in failedSagaList) {
            val strategy = sagaCompensationStrategyMapper.getStrategy(saga.sagaType)
            strategy.compensate(saga)
        }
    }

    /**
     * grpc saga 재시도
     */
    override suspend fun retryGrpcFailSagas() {
        val failedSagaList = sagaRepository.getFailedSagas()

        for (saga in failedSagaList) {
            val strategy = sagaGrpcCompensationStrategyMapper.getStrategy(saga.sagaType)
            strategy.compensate(saga)
        }
    }
}