package dev.concert.infrastructure

import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.infrastructure.jpa.ReservationJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ReservationRepositoryImpl (
    private val reservationJpaRepository: ReservationJpaRepository,
) : ReservationRepository {
    override fun saveReservation(reservation: ReservationEntity): ReservationEntity {
        return reservationJpaRepository.save(reservation)
    }

    override fun findById(reservationId: Long): ReservationEntity? {
        return reservationJpaRepository.findReservationInfo(reservationId)
    }

    override fun findExpiredReservations(): List<ReservationEntity> {
        return reservationJpaRepository.findExpiredReservations()
    }

    override fun updateReservationStatusToExpired(reservationIds: List<Long>) {
        reservationJpaRepository.updateReservationStatusToExpired(reservationIds)
    }

    override fun findAll() : List<ReservationEntity> {
        return reservationJpaRepository.findAll()
    }
}