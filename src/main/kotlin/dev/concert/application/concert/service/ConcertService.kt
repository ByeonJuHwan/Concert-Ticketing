package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertsDto

interface ConcertService {
    fun getConcerts(): List<ConcertsDto>
}