package dev.concert.domain.repository

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity

interface ConcertRepository {
    fun getConcerts(): List<ConcertEntity>
    fun saveConcert(concertEntity: ConcertEntity): ConcertEntity
    fun getAvailableDates(concertId: Long): List<ConcertOptionEntity>
    fun getAvailableSeats(concertOptionId: Long): List<SeatEntity>
    fun saveConcertOption(concertOptionEntity: ConcertOptionEntity): ConcertOptionEntity
    fun deleteAll()
    fun findById(id: Long): ConcertEntity?
}