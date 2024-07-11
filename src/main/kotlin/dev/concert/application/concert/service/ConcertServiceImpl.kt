package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.domain.ConcertRepository
import org.springframework.stereotype.Service

@Service
class ConcertServiceImpl (
    private val concertRepository: ConcertRepository,
) : ConcertService {
    override fun getConcerts(): List<ConcertsDto> {
        return concertRepository.getConcerts().map { ConcertsDto(
            id = it.id,
            concertName = it.concertName,
            singer = it.singer,
            startDate = it.startDate,
            endDate = it.endDate,
            reserveStartDate = it.reserveStartDate,
            reserveEndDate = it.reserveEndDate,
        ) }
    }
 
    override fun getAvailableDates(concertId: Long): List<ConcertDatesDto> { 
        return concertRepository.getAvailableDates(concertId).map { ConcertDatesDto( 
            concertId = it.concert.id, 
            concertName = it.concert.concertName, 
            availableSeats = it.availableSeats, 
            concertTime = it.concertTime, 
            concertVenue = it.concertVenue, 
            concertDate = it.concertDate, 
        )} 
    } 

    override fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatsDto> {
        return concertRepository.getAvailableSeats(concertOptionId).map { ConcertSeatsDto(
            seatId = it.id,
            seatNo = it.seatNo,
            price = it.price,
            status = it.seatStatus,
        ) }
    }
}
