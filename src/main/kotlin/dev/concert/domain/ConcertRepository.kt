package dev.concert.domain

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity

interface ConcertRepository {
    fun getConcerts(): List<ConcertEntity>
    fun getAvailableDates(concertId: Long): List<ConcertOptionEntity>
    fun getAvailableSeats(concertOptionId: Long): List<SeatEntity>
    fun getSeatWithLock(seatId: Long): SeatEntity?
}