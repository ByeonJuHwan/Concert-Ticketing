package dev.concert.application.payment

import dev.concert.application.payment.dto.PaymentDto
import dev.concert.application.payment.dto.PaymentResponseDto
import dev.concert.application.point.service.PointService
import dev.concert.application.reservation.ReservationService
import dev.concert.application.token.TokenService
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.exception.ReservationExpiredException
import org.springframework.stereotype.Component

@Component
class PaymentFacade(
    private val pointService: PointService,
    private val reservationService: ReservationService,
    private val tokenService: TokenService,
    private val paymentService: PaymentService,
) {
    fun pay(request: PaymentDto) : PaymentResponseDto {
        // 예약 정보를 가져온다
        val reservation = reservationService.getReservation(request.reservationId)

        // 임시 좌석 저장인 5분 안에 결재 요청을 했는지 확인한다 expiresAt 으로 확인(만료되었다면 상태를 Expired로 변경하고 예외를 터트린다)
        if(reservationService.isExpired(reservation)) {
            throw ReservationExpiredException("예약이 만료되었습니다.")
        }

        // 포인트 상태를 확인한다 (포인트가 price 보다 적으면 예외를 터트린다)
        // TODO N+1 문제 발생 쿼리 처리 필요
        val currentPoint = pointService.checkPoint(reservation.user, reservation.seat.price)

        // 포인트 차감 -> 결제를 진행한다 (카드 무통장등등 많지만 요구사항은 포인트 이므로 포인트로 진행)
        pointService.deductPoints(currentPoint, reservation.seat.price)

        // 결재정보를 저장한다
        paymentService.createPayments(reservation)

        // 예약 정보를 업데이트한다
        reservationService.changeReservationStatus(reservation, ReservationStatus.PAID)

        // 토큰을 삭제한다
        tokenService.deleteToken(reservation.user)

        return PaymentResponseDto(
            reservationId = reservation.id,
            seatNo = reservation.seat.seatNo,
            status = reservation.status,
            price = reservation.seat.price,
            // TODO 우선여기까지 하고 테스트 후 다음 더 생각
        )
    }
}