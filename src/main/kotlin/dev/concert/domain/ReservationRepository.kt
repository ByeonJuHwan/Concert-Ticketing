package dev.concert.domain

import dev.concert.domain.entity.ReservationEntity

interface ReservationRepository {
    fun reserveSeat(reservation: ReservationEntity): ReservationEntity
}