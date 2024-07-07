package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertsDto

interface ConcertService {
    fun getConcerts(): List<ConcertsDto>
    fun getAvailableDates(concertId: Long): List<ConcertDatesDto>
}