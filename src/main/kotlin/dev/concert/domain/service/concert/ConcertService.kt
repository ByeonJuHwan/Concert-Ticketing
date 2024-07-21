package dev.concert.domain.service.concert

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity

interface ConcertService {
    fun getConcerts(): List<ConcertEntity>
    fun getAvailableDates(concertId: Long): List<ConcertOptionEntity>
    fun getAvailableSeats(concertOptionId: Long): List<SeatEntity>
}