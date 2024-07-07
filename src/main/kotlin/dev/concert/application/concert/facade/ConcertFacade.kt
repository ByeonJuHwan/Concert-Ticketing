package dev.concert.application.concert.facade

import dev.concert.application.concert.dto.ConcertDatesDto
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
}