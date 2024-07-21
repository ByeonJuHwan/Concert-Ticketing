package dev.concert.domain.service.reservation

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity

interface ReservationService {
    fun saveReservation(user : UserEntity, seat : SeatEntity) : ReservationEntity
    fun getReservation(reservationId: Long): ReservationEntity
    fun isExpired(reservation: ReservationEntity) : Boolean
    fun changeReservationStatusPaid(reservation: ReservationEntity)
    fun manageReservationStatus()
    fun isPending(reservation: ReservationEntity)
}