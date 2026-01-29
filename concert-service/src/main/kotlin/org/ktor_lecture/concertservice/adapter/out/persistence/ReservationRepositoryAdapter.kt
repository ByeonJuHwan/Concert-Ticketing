package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ReservationJpaRepository
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ReservationRepositoryAdapter (
    private val reservationJpaRepository: ReservationJpaRepository,
): ReservationRepository {
    override fun save(reservation: ReservationEntity): ReservationEntity {
        return reservationJpaRepository.save(reservation)
    }

    override fun getReservation(reservationId: Long): Optional<ReservationEntity> {
        return reservationJpaRepository.findById(reservationId)
    }

    override fun getReservationWithSeatInfo(reservationId: Long): ReservationEntity? {
        return reservationJpaRepository.findReservationAndSeatInfo(reservationId)
    }

    override fun findExpiredReservations(): List<ReservationEntity> {
        return reservationJpaRepository.findExpiredReservations()
    }

    override fun updateReservationStatusToExpired(reservationIds: List<Long>) {
        reservationJpaRepository.updateReservationStatusToExpired(reservationIds)
    }

    override fun deleteAll() {
        reservationJpaRepository.deleteAll()
    }

    override fun searchUserReservations(userId: Long): List<ReservationEntity> {
        return reservationJpaRepository.searchUserReservations(userId)
    }
}