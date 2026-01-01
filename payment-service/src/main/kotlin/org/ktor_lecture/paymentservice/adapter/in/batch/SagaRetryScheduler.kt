package org.ktor_lecture.paymentservice.adapter.`in`.batch

import org.ktor_lecture.paymentservice.application.port.`in`.saga.RetryFailSagaUseCase
import org.ktor_lecture.paymentservice.common.DistributedLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SagaRetryScheduler (
    private val retryFailSagaUseCase: RetryFailSagaUseCase,
) {

    // 30초마다 스케줄러동작
    @DistributedLock(
        key = "saga-retry-lock",
    )
//    @Scheduled(fixedRate = 30000) http retry 스케줄러는 잠시 주석처리
    fun retryFailSaga() {
        retryFailSagaUseCase.retryFailSagas()
    }


    // 30초마다 스케줄러동작
    @DistributedLock(
        key = "saga-grpc-retry-lock",
    )
    @Scheduled(fixedRate = 30000)
    suspend fun retryGrpcFailSaga() {
        retryFailSagaUseCase.retryGrpcFailSagas()
    }
}