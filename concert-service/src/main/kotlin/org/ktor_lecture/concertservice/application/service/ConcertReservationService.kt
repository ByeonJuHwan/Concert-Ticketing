package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.adapter.`in`.web.response.ConcertReservationResponse
import org.ktor_lecture.concertservice.application.port.`in`.ChangeReservationPendingUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationExpiredUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationPaidUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchReservationUseCase
import org.ktor_lecture.concertservice.application.port.out.IdempotencyRepository
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.service.command.ChangeReservationPendingCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand
import org.ktor_lecture.concertservice.domain.annotation.ReadOnlyTransactional
import org.ktor_lecture.concertservice.domain.entity.IdempotencyEntity
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConcertReservationService (
    private val reservationRepository: ReservationRepository,
    private val idempotencyRepository: IdempotencyRepository,
): SearchReservationUseCase, ReservationExpiredUseCase, ReservationPaidUseCase, ChangeReservationPendingUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ReadOnlyTransactional
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
        val reservation = reservationRepository.getReservation(command.reservationId)
                    .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        reservation.changeStatus(ReservationStatus.PAID)
    }

    @Transactional
    override fun changeReservationPending(command: ChangeReservationPendingCommand) {
        val sagaId = command.sagaId

        val idempotency = idempotencyRepository.findBySagaId(sagaId)
        if (idempotency != null) {
            log.info("이미 처리된 예약 PENDING 변경 요청입니다. sagaId=$sagaId")
            return
        }

        val reservation = reservationRepository.getReservation(command.reservationId)
                            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        reservation.changeStatus(ReservationStatus.PENDING)

        idempotencyRepository.save(
            IdempotencyEntity(
                sagaId = sagaId,
            )
        )
    }
}