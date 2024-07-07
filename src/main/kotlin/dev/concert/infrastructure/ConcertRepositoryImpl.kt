package dev.concert.infrastructure

import dev.concert.domain.ConcertRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.infrastructure.jpa.ConcertJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ConcertRepositoryImpl (
    private val concertJpaRepository: ConcertJpaRepository
) : ConcertRepository {
    override fun getConcerts(): List<ConcertEntity> {
        return concertJpaRepository.findAllByStartDateAfter()
    }
}