package dev.concert.domain.repository

import dev.concert.domain.entity.SeatEntity

interface SeatRepository {
    fun getSeatWithLock(seatId: Long): SeatEntity?
    fun save(seat: SeatEntity) : SeatEntity
}