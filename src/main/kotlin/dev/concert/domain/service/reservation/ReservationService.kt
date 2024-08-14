package dev.concert.domain.service.reservation

import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.event.reservation.ReservationEvent

interface ReservationService {
    fun manageReservationStatus()
    fun createSeatReservation(user: UserEntity, seatId: Long): ReservationEntity
    fun getReservation(reservationId: Long): ReservationEntity
    fun saveMsgToOutBox(event: ReservationEvent)
    fun retryInitOrFailEvents()
    fun deleteOutBoxEvents()
    fun publishReservationEvent(event: ReservationEvent)
}