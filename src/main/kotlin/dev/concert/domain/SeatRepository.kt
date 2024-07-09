package dev.concert.domain

import dev.concert.domain.entity.SeatEntity

interface SeatRepository {
    fun getSeatWithLock(seatId: Long): SeatEntity?
}