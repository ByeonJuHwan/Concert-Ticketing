package dev.concert.infrastructure

import dev.concert.domain.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.infrastructure.jpa.ConcertJpaRepository
import dev.concert.infrastructure.jpa.ConcertOptionJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ConcertRepositoryImpl (
    private val concertJpaRepository: ConcertJpaRepository,
    private val concertOptionJpaRepository: ConcertOptionJpaRepository,
) : ConcertRepository {
    override fun getConcerts(): List<ConcertEntity> {
        return concertJpaRepository.findAllByStartDateAfter()
    }

    override fun getAvailableDates(concertId: Long): List<ConcertOptionEntity> {
        return concertOptionJpaRepository.findAvailableDates(concertId)
    }
}