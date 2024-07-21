package dev.concert.domain.service.reservation

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity

interface ReservationService {
    fun saveReservation(user : UserEntity, seat : SeatEntity) : ReservationEntity
    fun manageReservationStatus()
}