package dev.concert.domain.service.seat

import dev.concert.domain.entity.SeatEntity

interface SeatService {
    fun changeSeatStatusReserved(seat: SeatEntity)
    fun changeSeatStatusAvailable(seat: SeatEntity)
    fun checkAndReserveSeatTemporarily(seatId: Long): SeatEntity
}