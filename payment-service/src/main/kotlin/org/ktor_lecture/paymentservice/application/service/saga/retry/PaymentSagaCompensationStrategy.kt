package org.ktor_lecture.paymentservice.application.service.saga.retry

import org.ktor_lecture.paymentservice.application.port.out.http.ConcertApiClient
import org.ktor_lecture.paymentservice.application.port.out.http.PointApiClient
import org.ktor_lecture.paymentservice.application.port.out.SagaRepository
import org.ktor_lecture.paymentservice.application.service.PaymentCompensation
import org.ktor_lecture.paymentservice.application.service.PaymentService
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.PAYMENT_SAVE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.POINT_USE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.RESERVATION_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.SEAT_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.SagaType
import org.ktor_lecture.paymentservice.common.JsonUtil
import org.ktor_lecture.paymentservice.domain.entity.SagaEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PaymentSagaCompensationStrategy (
    private val paymentService: PaymentService,
    private val pointApiClient: PointApiClient,
    private val concertApiClient: ConcertApiClient,
    private val sagaRepository: SagaRepository,
): SagaCompensationStrategy {

    private val log : Logger = LoggerFactory.getLogger(PaymentSagaCompensationStrategy::class.java)

    /**
     * 이 전략이 지원하는 Saga Type인지 확인
     */
    override fun supports(sagaType: String): Boolean {
        return sagaType == SagaType.PAYMENT
    }

    /**
     * 재시도 보상 로직 구현
     */
    override fun compensate(saga: SagaEntity) {
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

        val payload: PaymentCompensation = JsonUtil.decodeFromJson<PaymentCompensation>(saga.payload!!)

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
                    POINT_USE -> pointApiClient.cancel(userId, pointHistoryId, price)
                    RESERVATION_CONFIRM -> concertApiClient.changeReservationPending(reservationId)
                    SEAT_CONFIRM -> concertApiClient.changeSeatTemporarilyAssigned(reservationId)
                    PAYMENT_SAVE -> paymentService.cancelPayment(paymentId, saga.id.toString())
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
}