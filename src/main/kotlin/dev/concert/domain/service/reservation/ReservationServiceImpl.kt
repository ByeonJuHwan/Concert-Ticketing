package dev.concert.domain.service.reservation

import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
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
    override fun saveReservation(user: UserEntity, seat: SeatEntity) : ReservationEntity {
        val expiresAt = LocalDateTime.now().plusMinutes(5)

        val reservation = ReservationEntity(
            user = user,
            seat = seat,
            expiresAt = expiresAt,
        )
        return reservationRepository.saveReservation(reservation)
    }

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
}