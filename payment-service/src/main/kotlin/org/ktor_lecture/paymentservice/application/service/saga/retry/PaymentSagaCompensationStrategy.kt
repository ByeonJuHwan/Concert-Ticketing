package org.ktor_lecture.paymentservice.application.service.saga.retry

import org.ktor_lecture.paymentservice.application.port.out.ConcertApiClient
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
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

    override fun supports(sagaType: String): Boolean {
        return sagaType == SagaType.PAYMENT
    }

    override fun compensate(saga: SagaEntity) {
        if (saga.payload == null) {
            log.warn("Saga payload가 존재하지 않습니다")
            return
        }

        val payload: PaymentCompensation = JsonUtil.decodeFromJson<PaymentCompensation>(saga.payload!!)

        val completedSteps = saga.getCompletedStepList()

        val userId = payload.userId
        val price = payload.price
        val requestId = payload.requestId
        val pointHistoryId = payload.historyId
        val paymentId = payload.paymentId

        completedSteps
            .reversed()
            .forEach { step ->
            try {
                when (step) {
                    POINT_USE -> pointApiClient.cancel(userId, pointHistoryId, price)
                    RESERVATION_CONFIRM -> concertApiClient.changeReservationPending(requestId)
                    SEAT_CONFIRM -> concertApiClient.changeSeatTemporarilyAssigned(requestId)
                    PAYMENT_SAVE -> paymentService.cancelPayment(paymentId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log.error("보상실패: $step - ${e.message}")
            }
        }

        saga.complete()
        sagaRepository.save(saga)
    }

}