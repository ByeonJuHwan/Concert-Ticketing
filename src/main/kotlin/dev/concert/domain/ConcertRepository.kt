package dev.concert.domain

import dev.concert.domain.entity.ConcertEntity

interface ConcertRepository {
    fun getConcerts(): List<ConcertEntity>
}