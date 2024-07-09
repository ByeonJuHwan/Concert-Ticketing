package dev.concert.infrastructure

import dev.concert.domain.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.infrastructure.jpa.ConcertJpaRepository
import dev.concert.infrastructure.jpa.ConcertOptionJpaRepository
import dev.concert.infrastructure.jpa.ConcertSeatJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ConcertRepositoryImpl (
    private val concertJpaRepository: ConcertJpaRepository,
    private val concertOptionJpaRepository: ConcertOptionJpaRepository,
    private val concertSeatJpaRepository: ConcertSeatJpaRepository,
) : ConcertRepository {
    override fun getConcerts(): List<ConcertEntity> {
        return concertJpaRepository.findAllByStartDateAfter()
    }

    override fun saveConcert(concertEntity: ConcertEntity): ConcertEntity {
        return concertJpaRepository.save(concertEntity)
    }

    override fun getAvailableDates(concertId: Long): List<ConcertOptionEntity> {
        return concertOptionJpaRepository.findAvailableDates(concertId)
    }

    override fun getAvailableSeats(concertOptionId: Long): List<SeatEntity> {
        return concertSeatJpaRepository.findAvailableSeats(concertOptionId)
    }
}