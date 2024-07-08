package dev.concert.application.concert.facade

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.application.concert.service.ConcertService
import org.springframework.stereotype.Service

@Service
class ConcertFacade (
    private val concertService: ConcertService
){
    fun getConcerts(): List<ConcertsDto> {
        return concertService.getConcerts()
    }

    fun getAvailableDates(concertId: Long): List<ConcertDatesDto> {
        return concertService.getAvailableDates(concertId)
    }

    fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatsDto> {
        return concertService.getAvailableSeats(concertOptionId)
    }

    fun reserveSeat(request: ConcertReservationDto): ConcertReservationResponseDto {
        return concertService.reserveSeat(request)
    }
}