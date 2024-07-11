package dev.concert.application.payment

import dev.concert.application.payment.dto.PaymentDto
import dev.concert.application.payment.dto.PaymentResponseDto
import dev.concert.application.point.service.PointHistoryService
import dev.concert.application.point.service.PointService
import dev.concert.application.reservation.ReservationService
import dev.concert.application.seat.SeatService
import dev.concert.application.token.TokenService
import dev.concert.exception.ReservationExpiredException
import org.springframework.stereotype.Component

@Component
class PaymentFacade(
    private val seatService: SeatService,
    private val pointService: PointService,
    private val reservationService: ReservationService,
    private val tokenService: TokenService,
    private val paymentService: PaymentService,
    private val pointHistoryService: PointHistoryService,
) {
    fun pay(request: PaymentDto) : PaymentResponseDto {
        // 예약 정보를 가져온다
        val reservation = reservationService.getReservation(request.reservationId)

        // 이미 결재 했는지 안했는지 확인한다
        paymentService.checkPayment(reservation)

        // 임시 좌석 저장인 5분 안에 결재 요청을 했는지 확인한다 expiresAt 으로 확인(만료되었다면 상태를 Expired로 변경하고 예외를 터트린다)
        if(reservationService.isExpired(reservation)) {
            seatService.changeSeatStatusAvailable(reservation.seat)
            throw ReservationExpiredException("예약이 만료되었습니다.")
        }

        // 포인트 상태를 확인한다 (포인트가 price 보다 적으면 예외를 터트린다)
        val currentPoint = pointService.checkPoint(reservation.user, reservation.seat.price)

        // 포인트 차감 -> 결제를 진행한다 (카드 무통장등등 많지만 요구사항은 포인트 이므로 포인트로 진행)
        pointService.deductPoints(currentPoint, reservation.seat.price)

        // 포인트 차감 히스토리 저장
        pointHistoryService.saveUsePointHistory(reservation.user, reservation.seat.price)

        // 결재정보를 저장한다
        paymentService.createPayments(reservation)

        // 예약 정보를 업데이트한다
        reservationService.changeReservationStatusPaid(reservation)

        // 좌석 정보 업데이트
        seatService.changeSeatStatusReserved(reservation.seat)

        // 토큰을 삭제한다
        tokenService.deleteToken(reservation.user)

        return PaymentResponseDto(
            reservationId = reservation.id,
            seatNo = reservation.seat.seatNo,
            status = reservation.status,
            price = reservation.seat.price,
        )
    }
}