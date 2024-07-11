package dev.concert.application.seat

import dev.concert.domain.entity.SeatEntity

interface SeatService {
    fun getSeat (seatId: Long): SeatEntity
    fun checkSeatAvailable(seat: SeatEntity)
    fun saveSeat(seatEntity: SeatEntity) : SeatEntity
    fun changeSeatStatusTemporary(seat: SeatEntity)
    fun changeSeatStatusReserved(seat: SeatEntity)
    fun changeSeatStatusAvailable(seat: SeatEntity)
}