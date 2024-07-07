package dev.concert.domain

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity

interface ConcertRepository {
    fun getConcerts(): List<ConcertEntity>
    fun getAvailableDates(concertId: Long): List<ConcertOptionEntity>
}