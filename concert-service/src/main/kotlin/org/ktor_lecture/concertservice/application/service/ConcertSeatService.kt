package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatReservedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatTemporarilyAssignedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SeatReservationAvailableUseCase
import org.ktor_lecture.concertservice.application.port.out.IdempotencyRepository
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.command.ChangeReservationTemporarilyAssignedCommand
import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand
import org.ktor_lecture.concertservice.domain.entity.IdempotencyEntity
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConcertSeatService (
    private val reservationRepository: ReservationRepository,
    private val seatRepository: SeatRepository,
    private val idempotencyRepository: IdempotencyRepository,
): ChangeSeatReservedUseCase, ChangeSeatTemporarilyAssignedUseCase, SeatReservationAvailableUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun changeSeatStatusReserved(command: ChangeSeatStatusReservedCommand) {
        val reservation = reservationRepository.getReservation(command.requestId.toLong())
                            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        val seat = seatRepository.getSeatWithLock(reservation.seat.id!!)
                    ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)

        seat.reserve()
    }

    @Transactional
    override fun changeSeatTemporarilyAssigned(command: ChangeReservationTemporarilyAssignedCommand) {
        val sagaId = command.sagaId
        val idempotency = idempotencyRepository.findBySagaId(sagaId)
        if (idempotency != null) {
            log.info("이미 처리된 좌석 TemporarilyAssigned 변경 요청입니다 sagaId=$sagaId")
            return
        }

        val reservation = reservationRepository.getReservation(command.reservationId)
                            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        val seat = seatRepository.getSeatWithLock(reservation.seat.id!!)
                    ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)

        seat.temporarilyAssign()

        idempotencyRepository.save(
            IdempotencyEntity(
                sagaId = sagaId,
            )
        )
    }

    @Transactional
    override fun seatReservationAvailable() {
        val expiredReservations = reservationRepository.findExpiredReservations()

        if (expiredReservations.isEmpty()) {
            return
        }

        val reservationIds = expiredReservations.map { it.id!! }
        val seatIds = expiredReservations.map { it.seat.id!! }

        if (reservationIds.isNotEmpty() && seatIds.isNotEmpty()) {
            reservationRepository.updateReservationStatusToExpired(reservationIds)
            seatRepository.updateSeatStatusToAvailable(seatIds)
        }
    }
}