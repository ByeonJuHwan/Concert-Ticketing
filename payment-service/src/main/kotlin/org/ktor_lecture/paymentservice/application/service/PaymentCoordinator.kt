package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUseCase
import org.ktor_lecture.paymentservice.application.port.out.ConcertApiClient
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.PAYMENT_SAVE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.POINT_USE
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.RESERVATION_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.PaymentSagaStep.SEAT_CONFIRM
import org.ktor_lecture.paymentservice.application.service.saga.SagaExecution
import org.ktor_lecture.paymentservice.application.service.saga.SagaType.PAYMENT
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.ktor_lecture.paymentservice.domain.status.ReservationStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
class PaymentCoordinator (
    private val paymentService: PaymentService,
    private val pointApiClient: PointApiClient,
    private val concertApiClient: ConcertApiClient,
    private val sagaExecution: SagaExecution,
) :PaymentUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 결제 처리
     *
     * 1. 예약 정보 조회
     * 2. 이미 결재한 예약인지 확인한다 (이미 결재 혹은 만료된 예약은 예외를 발생시킨다)
     * 3. 임시 좌석 저장인 5분 안에 결재 요청을 했는지 확인한다 expiresAt 으로 확인(만료되었다면 상태를 Expired로 변경하고 예외를 터트린다)
     * 4. 포인트 상태를 확인한다 (포인트가 price 보다 적으면 예외를 터트린다)
     * 5. 포인트 차감 -> 결제를 진행한다 (카드 무통장등등 많지만 요구사항은 포인트 이므로 포인트로 진행)
     * 6. 포인트 차감 히스토리 저장
     * 7. 예약 상태 변경 -> 결제 완료로 변경
     * 8. 좌석 상태 변경 -> 예약 완료로 변경
     * 9. 예약 정보 저장
     */
    override fun pay(command: PaymentCommand): PaymentResponse {
        val reservation = concertApiClient.getReservation(command.reservationId)
        validateReservation(reservation)


        val reservationId = reservation.reservationId.toString()
        val userId = reservation.userId.toString()
        var paymentId = 0L
        var pointHistoryId = 0L

        val sagaId = sagaExecution.setInitSaga(PAYMENT)

        try {

            // 포인트 차감
            val pointResponse = sagaExecution.executeStep(
                sagaId,
                POINT_USE
            ) {
                pointApiClient.use(
                    userId = userId,
                    amount = reservation.price,
                )
            }
            pointHistoryId = pointResponse.pointHistoryId

            // 예약 확정
            sagaExecution.executeStep(sagaId, RESERVATION_CONFIRM) {
                concertApiClient.changeReservationPaid(reservationId)
            }

            // 좌석 확정
            sagaExecution.executeStep(sagaId, SEAT_CONFIRM) {
                concertApiClient.changeSeatReserved(reservationId)
            }

            // 결제 저장
            val payment: PaymentEntity = sagaExecution.executeStep(sagaId, PAYMENT_SAVE) {
                paymentService.save(
                    PaymentCreateCommand(reservation.price)
                )
            }
            paymentId = payment.id!!

            sagaExecution.completeSaga(sagaId)

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
                requestId = reservationId,
                paymentId = paymentId
            )
            throw ConcertException(ErrorCode.PAYMENT_FAILED)
        }
    }

    private fun handleRollback(sagaId: Long, userId: String, price: Long, pointHistoryId: Long, requestId: String, paymentId: Long) {
        sagaExecution.startCompensation(sagaId)

        val completedSteps = sagaExecution.getCompletedSteps(sagaId)

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
                log.error("보상실패: $step - ${e.message}")
            }
        }

        // 보상 완료 표시
        sagaExecution.completeCompensation(sagaId)
    }


    private fun validateReservation(reservation: ConcertReservationResponse) {
        checkReservationStatus(reservation.status)

        if (isExpired(reservation.expiresAt)) {
            concertApiClient.reservationExpiredAndSeatAvaliable(reservation.reservationId)
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
}