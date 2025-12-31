package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.`in`.grpc.PaymentGrpcUseCase
import org.ktor_lecture.paymentservice.application.port.out.grpc.ConcertGrpcClient
import org.ktor_lecture.paymentservice.application.port.out.grpc.PointGrpcClient
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.ktor_lecture.paymentservice.domain.status.ReservationStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PaymentGrpcCoordinator (
    private val concertGrpcClient: ConcertGrpcClient,
    private val pointGrpcClient: PointGrpcClient,
    private val paymentService: PaymentService,
): PaymentGrpcUseCase {


    override suspend fun pay(command: PaymentCommand): PaymentResponse {
        val reservationId = command.reservationId

        val reservation = concertGrpcClient.getReservation(reservationId)
        validateReservation(reservation)

        // 포인트 차감
        pointGrpcClient.use(
            userId = reservation.userId,
            amount = reservation.price,
        )

        // 예약 확정
        concertGrpcClient.changeReservationPaid(
            reservationId = reservationId,
        )

        // 좌석 확정
        concertGrpcClient.changeSeatReserved(
            reservationId = reservationId,
        )

        // 결제 저장
        val payment = paymentService.save(
            PaymentCreateCommand(reservation.price)
        )

        return PaymentResponse(
            reservationId = reservation.reservationId,
            seatNo = reservation.seatNo,
            status = payment.paymentStatus.toString(),
            price = reservation.price,
        )
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
}