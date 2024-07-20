package dev.concert.application.reservation

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.ReservationStatus
import java.time.LocalDateTime

interface ReservationService {
    fun saveReservation(user : UserEntity, seat : SeatEntity) : ReservationEntity
    fun getReservation(reservationId: Long): ReservationEntity
    fun isExpired(reservation: ReservationEntity) : Boolean
    fun changeReservationStatusPaid(reservation: ReservationEntity)
    fun manageReservationStatus()
    fun isPending(reservation: ReservationEntity)
}