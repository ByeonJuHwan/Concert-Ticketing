package dev.concert.application.reservation

import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.exception.ReservationAlreadyPaidException
import dev.concert.exception.ReservationExpiredException
import dev.concert.exception.ReservationNotFoundException
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

    @Transactional(readOnly = true)
    override fun getReservation(reservationId: Long): ReservationEntity {
        return reservationRepository.findById(reservationId)?: throw ReservationNotFoundException(" 예약 정보를 찾을 수 없습니다.")
    }

    @Transactional
    override fun isExpired(reservation: ReservationEntity) : Boolean {
        if(LocalDateTime.now().isAfter(reservation.expiresAt)){
            reservation.changeStatus(ReservationStatus.EXPIRED)
            reservationRepository.saveReservation(reservation)
            return true
        }
        return false
    }

    @Transactional
    override fun changeReservationStatusPaid(reservation: ReservationEntity) {
        reservation.changeStatus(ReservationStatus.PAID)
        reservationRepository.saveReservation(reservation)
    }

    @Transactional
    override fun manageReservationStatus() {
        // 예약을 전부 가져와
        reservationRepository.findExpiredReservations().forEach{
            it.changeStatus(ReservationStatus.EXPIRED)
            it.seat.changeSeatStatus(SeatStatus.AVAILABLE)
        }
    }

    override fun isPending(reservation: ReservationEntity) {
        when (reservation.status) {
            ReservationStatus.PAID -> throw ReservationAlreadyPaidException("이미 결제된 예약입니다.")
            ReservationStatus.EXPIRED -> throw ReservationExpiredException("만료된 예약입니다.")
            else -> return
        }
    }
}