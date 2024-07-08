package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto

interface ConcertService {
    fun getConcerts(): List<ConcertsDto>
    fun getAvailableDates(concertId: Long): List<ConcertDatesDto>
    fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatsDto>
    fun reserveSeat(request: ConcertReservationDto): ConcertReservationResponseDto
}