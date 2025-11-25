package org.ktor_lecture.paymentservice.application.service.saga

import org.ktor_lecture.paymentservice.application.port.out.SagaRepository
import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.ktor_lecture.paymentservice.domain.entity.SagaStatus
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SagaExecution (
    private val sagaRepository: SagaRepository,
): SagaStep {

    @Transactional
    override fun setInitSaga(sataType: String): Long {
        val saga = SagaEntity(
            sagaType = sataType,
            status = SagaStatus.IN_PROGRESS,
        )

        return sagaRepository.save(saga).id!!
    }

    @Transactional
    override fun <T> executeStep(sagaId: Long, stepName: String, action: () -> T): T {
        val saga = findSagaById(sagaId)

        try {
            saga.currentStep = stepName
            sagaRepository.save(saga)

            val result = action()

            saga.addCompletedStep(stepName)
            sagaRepository.save(saga)

            return result
        } catch (e: Exception) {
            saga.failedStep = stepName
            saga.failed()
            sagaRepository.save(saga)
            throw e
        }
    }

    @Transactional
    override fun completeSaga(sagaId: Long) {
        val saga = findSagaById(sagaId)

        saga.complete()
        sagaRepository.save(saga)
    }

    override fun getCompletedSteps(sagaId: Long): List<String> {
        return findSagaById(sagaId).getCompletedStepList()
    }

    @Transactional
    override fun startCompensation(sagaId: Long) {
        val saga = findSagaById(sagaId)

        saga.compensating()
        sagaRepository.save(saga)
    }

    @Transactional
    override fun completeCompensation(sagaId: Long) {
        val saga = findSagaById(sagaId)

        saga.compensated()
        sagaRepository.save(saga)
    }

    private fun findSagaById(sagaId: Long): SagaEntity {
        return sagaRepository.findById(sagaId)
                            .orElseThrow { ConcertException(ErrorCode.SAGA_NOT_FOUND) }
    }
}
