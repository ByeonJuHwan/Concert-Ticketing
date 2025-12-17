package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatReservedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ChangeSeatTemporarilyAssignedUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SeatReservationAvailableUseCase
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
): ChangeSeatReservedUseCase, ChangeSeatTemporarilyAssignedUseCase, SeatReservationAvailableUseCase {

    /**
     * 좌석의 상태를 예약상태로 변경한다
     */
    @Transactional
    override fun changeSeatStatusReserved(command: ChangeSeatStatusReservedCommand) {
        val reservation = reservationRepository.getReservation(command.reservationId)
                            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        val seat = seatRepository.getSeatWithLock(reservation.seat.id!!)
                    ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)

        seat.reserve()
    }

    /**
     * 예약 상태의 좌석을 임시 예약 상태로 변경한다
     */
    @Transactional
    override fun changeSeatTemporarilyAssigned(command: ChangeReservationTemporarilyAssignedCommand) {
        val reservation = reservationRepository.getReservation(command.reservationId)
                            .orElseThrow { throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND) }

        val seat = seatRepository.getSeatWithLock(reservation.seat.id!!)
                    ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)

        seat.temporarilyAssign()
    }

    /**
     * 예약 만료 기간이 지난 예약의 상태는 만료로 변경하고
     * 좌석의 상태는 다시 예약 가능으로 변경한다
     */
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