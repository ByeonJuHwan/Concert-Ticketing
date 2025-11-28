package org.ktor_lecture.paymentservice.adapter.`in`.batch

import org.ktor_lecture.paymentservice.application.port.`in`.saga.RetryFailSagaUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SagaRetryScheduler (
    private val retryFailSagaUseCase: RetryFailSagaUseCase,
) {

    // 30초마다 스케줄러동작
    @Scheduled(fixedRate = 60000)
    fun retryFailSaga() {
        retryFailSagaUseCase.retryFailSagas()
    }

}