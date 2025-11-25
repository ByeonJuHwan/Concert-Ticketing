package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUseCase
import org.ktor_lecture.paymentservice.application.port.out.ConcertApiClient
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PaymentCoordinator (
    private val paymentService: PaymentService,
    private val pointApiClient: PointApiClient,
    private val concertApiClient: ConcertApiClient,
) :PaymentUseCase {


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
        val reservation = concertApiClient.getReservation(command.reservationId) // TODO api 요청이라 try-catch 필요해보임

        checkReservationStatus(reservation.status)

        if (isExpired(reservation.expiresAt)) {
            concertApiClient.reservationExpiredAndSeatAvaliable(reservation.reservationId)
            throw ConcertException(ErrorCode.RESERVATION_EXPIRED)
        }

        // --- validation 종료
        val requestId: String = reservation.reservationId.toString()
        val userId: String = reservation.userId.toString()
        var pointUsed = false
        var reservationConfirmed = false
        var seatConfirmed = false

        try {
            // 포인트 사용 요청
            pointApiClient.use(
                requestId = requestId,
                userId = userId,
                amount = reservation.price
            )
            pointUsed = true

            // 예약 상태를 PAID로 변경
            concertApiClient.changeReservationPaid(requestId)
            reservationConfirmed = true

            // 좌석 상태를 Reserve 로 변경
            concertApiClient.changeSeatReserved(requestId)
            seatConfirmed = true

            // 결제 정보 저장
            val command = PaymentCreateCommand(
                price = reservation.price,
            )

            val status = paymentService.save(command)

            return PaymentResponse(
                reservationId = reservation.reservationId,
                seatNo = reservation.seatNo,
                status = status,
                price = reservation.price,
            )
        } catch (e: Exception) {
            handleTccRollback(
                pointUsed,
                reservationConfirmed,
                seatConfirmed,
                userId,
                reservation.price,
                requestId,
            )
            e.printStackTrace()
            throw ConcertException(ErrorCode.PAYMENT_FAILED)
        }
    }

    private fun handleTccRollback(pointUsed: Boolean, reservationConfirmed: Boolean, seatConfirmed: Boolean, userId: String, price: Long, requestId: String) {
        if (pointUsed) {
            // TODO 포인트 히스토리는 어떻게 하지?
            try {
                pointApiClient.cancel(userId, price)
            } catch (e: Exception) {

            }
        }

        if (reservationConfirmed) {
            try {
                concertApiClient.changeReservationPending(requestId)
            } catch (e: Exception) {

            }
        }

        if (seatConfirmed) {
            try {
                concertApiClient.changeSeatTemporarilyAssigned(requestId)
            } catch (e: Exception) {

            }
        }
    }


    private fun isExpired(expiresAt: LocalDateTime): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    private fun checkReservationStatus(status: String) {
        when (status) { // TODO 여기도 하드코딩 설정 필요해보임
            "PAID" -> throw ConcertException(ErrorCode.RESERVATION_ALREADY_PAID)
            "EXPIRED" -> throw ConcertException(ErrorCode.RESERVATION_EXPIRED)
            else -> return
        }
    }
}