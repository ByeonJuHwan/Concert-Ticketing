package dev.concert.domain.service.reservation

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.UserEntity

interface ReservationService {
    fun manageReservationStatus()
    fun createSeatReservation(user: UserEntity, seatId: Long): ReservationEntity
}