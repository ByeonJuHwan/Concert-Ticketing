package org.ktor_lecture.paymentservice.application.service.saga

interface SagaStep {

    fun <T> executeStep(sagaId: Long, stepName: String, action: () -> T): T
    fun setInitSaga(sataType: String): Long
    fun completeSaga(sagaId: Long)
    fun getCompletedSteps(sagaId: Long): List<String>
    fun startCompensation(sagaId: Long, payload: String)
    fun completeCompensation(sagaId: Long)
    fun increaseRetryCount(sagaId: Long)
    fun isRetryAvailable(sagaId: Long): Boolean
}