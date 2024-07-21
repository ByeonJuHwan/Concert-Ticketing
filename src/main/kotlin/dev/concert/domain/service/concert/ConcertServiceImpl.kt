package dev.concert.domain.service.concert

import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.repository.ConcertRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConcertServiceImpl (
    private val concertRepository: ConcertRepository,
) : ConcertService {

    @Transactional(readOnly = true)
    override fun getConcerts(): List<ConcertEntity> = concertRepository.getConcerts()

    @Transactional(readOnly = true)
    override fun getAvailableDates(concertId: Long): List<ConcertOptionEntity>
            = concertRepository.getAvailableDates(concertId)

    @Transactional(readOnly = true)
    override fun getAvailableSeats(concertOptionId: Long): List<SeatEntity>
            = concertRepository.getAvailableSeats(concertOptionId)
}