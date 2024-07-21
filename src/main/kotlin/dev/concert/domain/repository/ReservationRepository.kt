package dev.concert.domain.repository

import dev.concert.domain.entity.ReservationEntity

interface ReservationRepository {
    fun saveReservation(reservation: ReservationEntity): ReservationEntity
    fun findById(reservationId : Long) : ReservationEntity?
    fun findExpiredReservations(): List<ReservationEntity>
    fun updateReservationStatusToExpired(reservationIds: List<Long>)
}