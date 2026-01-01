package org.ktor_lecture.paymentservice.application.port.`in`.saga

interface RetryFailSagaUseCase {
    fun retryFailSagas()
    suspend fun retryGrpcFailSagas()
}