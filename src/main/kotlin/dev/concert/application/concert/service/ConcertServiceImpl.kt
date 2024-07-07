package dev.concert.application.concert.service

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
}