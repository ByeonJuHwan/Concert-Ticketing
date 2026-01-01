package org.ktor_lecture.paymentservice.application.service

import kotlinx.serialization.Serializable
import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.`in`.grpc.PaymentGrpcUseCase
import org.ktor_lecture.paymentservice.application.port.out.grpc.ConcertGrpcClient
import org.ktor_lecture.paymentservice.application.port.out.grpc.PointGrpcClient
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.PAYMENT_SAVE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.POINT_USE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.RESERVATION_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.SEAT_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.SagaExecution
import org.ktor_lecture.paymentservice.application.service.saga.SagaGrpcExecution
import org.ktor_lecture.paymentservice.application.service.saga.SagaStep
import org.ktor_lecture.paymentservice.application.service.saga.SagaType.PAYMENT
import org.ktor_lecture.paymentservice.common.JsonUtil
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.ktor_lecture.paymentservice.domain.status.ReservationStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PaymentGrpcCoordinator (
    private val concertGrpcClient: ConcertGrpcClient,
    private val pointGrpcClient: PointGrpcClient,
    private val paymentService: PaymentService,
    private val sagaGrpcExecution: SagaGrpcExecution,
): PaymentGrpcUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun pay(command: PaymentCommand): PaymentResponse {
        val reservationId = command.reservationId

        val reservation = concertGrpcClient.getReservation(reservationId)
        validateReservation(reservation)

        val userId = reservation.userId
        var paymentId = 0L
        var pointHistoryId = 0L

        val sagaId = sagaGrpcExecution.setInitSaga(PAYMENT)

        try {
            // 포인트 차감
            val pointResponse = sagaGrpcExecution.executeStep(
                sagaId = sagaId,
                stepName = POINT_USE,
            ) {
                pointGrpcClient.use(
                    userId = userId,
                    amount = reservation.price,
                )
            }

            pointHistoryId = pointResponse.pointHistoryId

            // 예약 확정
            sagaGrpcExecution.executeStep(
                sagaId = sagaId,
                stepName = RESERVATION_CONFIRM,
            ) {
                concertGrpcClient.changeReservationPaid(
                    reservationId = reservationId,
                )
            }

            // 좌석 확정
            sagaGrpcExecution.executeStep(
                sagaId = sagaId,
                stepName = SEAT_CONFIRM,
            ) {
                concertGrpcClient.changeSeatReserved(
                    reservationId = reservationId,
                )
            }

            // 결제 저장
            val payment: PaymentEntity = sagaGrpcExecution.executeStep(
                sagaId = sagaId,
                stepName = PAYMENT_SAVE,
            ) {
                paymentService.save(
                    PaymentCreateCommand(reservation.price)
                )
            }

            paymentId = payment.id!!

            sagaGrpcExecution.completeSaga(sagaId)

            return PaymentResponse(
                reservationId = reservation.reservationId,
                seatNo = reservation.seatNo,
                status = payment.paymentStatus.toString(),
                price = reservation.price,
            )
        } catch (e: Exception) {
            handleRollback(
                sagaId = sagaId,
                userId = userId,
                price = reservation.price,
                pointHistoryId = pointHistoryId,
                reservationId = reservationId,
                paymentId = paymentId
            )

            log.error("결제처리 실패 롤백처리 완료: ${e.message}")
            throw ConcertException(ErrorCode.PAYMENT_FAILED)
        }
    }

    private suspend fun handleRollback(sagaId: Long, userId: Long, price: Long, pointHistoryId: Long, reservationId: Long, paymentId: Long) {
        val payload = JsonUtil.encodeToJson(
            PaymentGrpcCompensation(
                sagaId = sagaId,
                userId = userId,
                price = price,
                historyId = pointHistoryId,
                reservationId = reservationId,
                paymentId = paymentId,
            )
        )

        sagaGrpcExecution.startCompensation(sagaId, payload)

        val completedSteps = sagaGrpcExecution.getCompletedSteps(sagaId)
        
        completedSteps
            .reversed()
            .forEach { step ->
            try {
                when (step) {
                    POINT_USE -> pointGrpcClient.cancel(userId, pointHistoryId, price, createSagaKey(sagaId, step))
                    RESERVATION_CONFIRM -> concertGrpcClient.changeReservationPending(reservationId, createSagaKey(sagaId, step))
                    SEAT_CONFIRM -> concertGrpcClient.changeSeatTemporarilyAssigned(reservationId, createSagaKey(sagaId, step))
                    PAYMENT_SAVE -> paymentService.cancelPayment(paymentId, createSagaKey(sagaId, step))
                }
            } catch (e: Exception) {
                log.error("보상실패: $step - ${e.message}")
                throw e
            }
        }

        // 보상 완료 표시
        sagaGrpcExecution.completeCompensation(sagaId)
    }


    private suspend fun validateReservation(reservation: ConcertReservationResponse) {
        checkReservationStatus(reservation.status)

        if (isExpired(reservation.expiresAt)) {
            concertGrpcClient.reservationExpiredAndSeatAvaliable(reservation.reservationId)
            throw ConcertException(ErrorCode.RESERVATION_EXPIRED)
        }
    }

    private fun isExpired(expiresAt: LocalDateTime): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    private fun checkReservationStatus(status: String) {
        when (status) {
            ReservationStatus.PAID.name -> throw ConcertException(ErrorCode.RESERVATION_ALREADY_PAID)
            ReservationStatus.EXPIRED.name -> throw ConcertException(ErrorCode.RESERVATION_EXPIRED)
            else -> return
        }
    }

    private fun createSagaKey(sagaId: Long, stepName: String): String {
        return sagaId.toString() + "_" + stepName
    }
}

@Serializable
data class PaymentGrpcCompensation(
    val sagaId: Long,
    val userId: Long,
    val price: Long,
    val historyId: Long,
    val reservationId: Long,
    val paymentId: Long,
)