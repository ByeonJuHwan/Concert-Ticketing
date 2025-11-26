package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatReservedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatTemporarilyAssignedUseCase
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.command.ChangeReservationTemporarilyAssignedCommand
import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConcertSeatService (
    private val reservationRepository: ReservationRepository,
    private val seatRepository: SeatRepository,
): ChangeSeatReservedUseCase, ChangeSeatTemporarilyAssignedUseCase {

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
        val reservation = reservationRepository.getReservation(command.requestId.toLong())
                            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        val seat = seatRepository.getSeatWithLock(reservation.seat.id!!)
                    ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)

        seat.temporarilyAssign()
    }
}