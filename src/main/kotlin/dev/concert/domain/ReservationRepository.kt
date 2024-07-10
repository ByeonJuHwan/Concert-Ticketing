package dev.concert.domain

import dev.concert.domain.entity.ReservationEntity

interface ReservationRepository {
    fun saveReservation(reservation: ReservationEntity): ReservationEntity
    fun findById(reservationId : Long) : ReservationEntity?
}