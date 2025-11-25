package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertReservationResponse
import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatReservedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationExpiredUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationPaidUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchReservationUseCase
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConcertReservationService (
    private val reservationRepository: ReservationRepository,
): SearchReservationUseCase, ReservationExpiredUseCase, ReservationPaidUseCase {
    override fun getReservation(reservationId: Long): ConcertReservationResponse {
        val reservation = reservationRepository.getReservationWithSeatInfo(reservationId)
            ?: throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND)

        return ConcertReservationResponse(
            reservationId = reservation.id!!,
            userId = reservation.user.id!!,
            seatId = reservation.seat.id!!,
            status = reservation.status.toString(),
            expiresAt = reservation.expiresAt,
            price = reservation.seat.price,
            seatNo = reservation.seat.seatNo,
        )
    }

    @Transactional
    override fun reservationExpiredAndSeatAvaliable(command: ReservationExpiredCommand) {
        val reservation = reservationRepository.getReservation(command.reservationId)
            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        reservation.changeStatus(ReservationStatus.EXPIRED)
        reservation.seat.changeStatus(SeatStatus.AVAILABLE)
    }

    @Transactional
    override fun changeReservationPaid(command: ReservationPaidCommand) {
        val reservation = reservationRepository.getReservation(command.requestId.toLong())
                    .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        reservation.changeStatus(ReservationStatus.PAID)
    }
}