package dev.concert.application.seat

import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.status.SeatStatus

interface SeatService {
    fun getSeat (seatId: Long): SeatEntity
    fun checkSeatAvailable(seat: SeatEntity)
    fun changeSeatStatus(seat: SeatEntity, temporarilyAssigned: SeatStatus)
    fun saveSeat(seatEntity: SeatEntity) : SeatEntity
}