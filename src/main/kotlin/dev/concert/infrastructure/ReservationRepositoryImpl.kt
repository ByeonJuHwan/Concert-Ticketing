package dev.concert.infrastructure

import dev.concert.domain.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.infrastructure.jpa.ReservationJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ReservationRepositoryImpl (
    private val reservationJpaRepository: ReservationJpaRepository,
) : ReservationRepository{
    override fun saveReservation(reservation: ReservationEntity): ReservationEntity {
        return reservationJpaRepository.save(reservation)
    }

    override fun findById(reservationId: Long): ReservationEntity? {
        return reservationJpaRepository.findReservationInfo(reservationId)
    }
 
    override fun findExpiredReservations(): List<ReservationEntity> { 
        return reservationJpaRepository.findExpiredReservations() 
    } 
} 
