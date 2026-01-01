package org.ktor_lecture.paymentservice.application.service.saga.retry

import org.ktor_lecture.paymentservice.application.port.out.SagaRepository
import org.ktor_lecture.paymentservice.application.port.out.grpc.ConcertGrpcClient
import org.ktor_lecture.paymentservice.application.port.out.grpc.PointGrpcClient
import org.ktor_lecture.paymentservice.application.service.PaymentGrpcCompensation
import org.ktor_lecture.paymentservice.application.service.PaymentService
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.PAYMENT_SAVE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.POINT_USE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.RESERVATION_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.SEAT_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.SagaType
import org.ktor_lecture.paymentservice.common.JsonUtil
import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PaymentGrpcSagaCompensationStrategy (
    private val paymentService: PaymentService,
    private val pointGrpcClient: PointGrpcClient,
    private val concertGrpcClient: ConcertGrpcClient,
    private val sagaRepository: SagaRepository,
): SagaGrpcCompensationStrategy {

    private val log : Logger = LoggerFactory.getLogger(PaymentGrpcSagaCompensationStrategy::class.java)

    /**
     * 이 전략이 지원하는 Saga Type인지 확인
     */
    override fun supports(sagaType: String): Boolean {
        return sagaType == SagaType.PAYMENT
    }

    /**
     * 재시도 보상 로직 구현
     */
    override suspend fun compensate(saga: SagaEntity) {
        if (saga.payload == null) {
            log.warn("Saga payload가 존재하지 않습니다")
            return
        }

        val retryAvailable = saga.isRetryAvailable()

        if(!retryAvailable) {
            // 개발자에게 알림 발송
            sendAlert(saga)
            return
        }

        val payload: PaymentGrpcCompensation = JsonUtil.decodeFromJson<PaymentGrpcCompensation>(saga.payload!!)

        val sagaId = payload.sagaId
        val userId = payload.userId
        val price = payload.price
        val reservationId = payload.reservationId
        val pointHistoryId = payload.historyId
        val paymentId = payload.paymentId

        val completedSteps = saga.getCompletedStepList()

        var allSuccess = true

        completedSteps
            .reversed()
            .forEach { step ->
            try {
                when (step) {
                    POINT_USE -> pointGrpcClient.cancel(userId, pointHistoryId, price, createSagaKey(sagaId, step))
                    RESERVATION_CONFIRM -> concertGrpcClient.changeReservationPending(reservationId, createSagaKey(sagaId, step))
                    SEAT_CONFIRM -> concertGrpcClient.changeSeatTemporarilyAssigned(reservationId, createSagaKey(sagaId, step))
                    PAYMENT_SAVE -> paymentService.cancelPayment(paymentId, createSagaKey(sagaId, step))
                    else -> throw ConcertException(ErrorCode.UNKNOWN_STEP)
                }
            } catch (e: Exception) {
                allSuccess = false
                log.error("보상실패: $step - $e")
            }
        }


        checkStepCompleted(saga, allSuccess)

        sagaRepository.save(saga)
    }

    private fun sendAlert(saga: SagaEntity) {
        log.error("보상재시도 불가 알림 발송: ${saga.id}")
        saga.alert()
        sagaRepository.save(saga)
    }

    private fun checkStepCompleted(saga: SagaEntity, success: Boolean) {
        if (success) {
            saga.complete()
        } else {
            saga.increaseRetryCount()
        }
    }

    private fun createSagaKey(sagaId: Long, stepName: String): String {
        return sagaId.toString() + "_" + stepName
    }
}