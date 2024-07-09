package dev.concert.application.reservation

import dev.concert.domain.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationServiceImpl (
    private val reservationRepository : ReservationRepository,
) : ReservationService{

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
}