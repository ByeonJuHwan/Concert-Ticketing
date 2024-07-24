package dev.concert.domain.service.reservation

import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import dev.concert.domain.repository.SeatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationServiceImpl (
    private val reservationRepository : ReservationRepository,
    private val seatRepository: SeatRepository,
) : ReservationService {

    @Transactional
    override fun manageReservationStatus() {
        val expiredReservations = reservationRepository.findExpiredReservations()

        val reservationIds = expiredReservations.map { it.id }
        val seatIds = expiredReservations.map { it.seat.id }

        if (reservationIds.isNotEmpty() && seatIds.isNotEmpty()) {
            reservationRepository.updateReservationStatusToExpired(reservationIds)
            seatRepository.updateSeatStatusToAvailable(seatIds)
        }
    }

    @Transactional
    override fun createSeatReservation(user: UserEntity, seatId: Long): ReservationEntity {
        val seat = getSeat(seatId)
        changeSeatStatusTemporary(seat)
        return saveReservation(user, seat)
    }

    private fun saveReservation(user: UserEntity, seat: SeatEntity) : ReservationEntity {
        val expiresAt = LocalDateTime.now().plusMinutes(5)

        val reservation = ReservationEntity(
            user = user,
            seat = seat,
            expiresAt = expiresAt,
        )
        return reservationRepository.saveReservation(reservation)
    }

    private fun getSeat(seatId: Long): SeatEntity {
        return seatRepository.getSeatWithLock(seatId) ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)
    }

    private fun changeSeatStatusTemporary(seat: SeatEntity) {
        checkSeatAvailable(seat)
        seat.changeSeatStatus(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    private fun checkSeatAvailable(seat: SeatEntity) {
        if (seat.seatStatus != SeatStatus.AVAILABLE){
            throw ConcertException(ErrorCode.SEAT_NOT_AVAILABLE)
        }
    }
}