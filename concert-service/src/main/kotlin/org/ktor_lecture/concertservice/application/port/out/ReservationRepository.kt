package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import java.util.Optional

interface ReservationRepository {
    fun save(reservation: ReservationEntity): ReservationEntity
    fun getReservation(reservationId: Long): Optional<ReservationEntity>
    fun getReservationWithSeatInfo(reservationId: Long): ReservationEntity?
    fun findExpiredReservations(): List<ReservationEntity>
    fun updateReservationStatusToExpired(reservationIds: List<Long>)
}