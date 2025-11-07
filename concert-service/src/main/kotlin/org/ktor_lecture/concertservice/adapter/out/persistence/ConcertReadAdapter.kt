package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertOptionJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertSeatJpaRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.springframework.stereotype.Component

@Component
class ConcertReadAdapter (
    private val concertJpaRepository: ConcertJpaRepository,
    private val concertOptionJpaRepository: ConcertOptionJpaRepository,
    private val concertSeatJpaRepository: ConcertSeatJpaRepository,
): ConcertReadRepository {
    override fun getConcerts(): List<ConcertEntity> {
        return concertJpaRepository.findAll()
    }

    override fun getAvailableDates(concertId: Long): List<ConcertOptionEntity> {
        return concertOptionJpaRepository.findAvailableDates(concertId)
    }

    override fun getAvailableSeats(concertOptionId: Long): List<SeatEntity> {
        return concertSeatJpaRepository.findByConcertOptionId(concertOptionId)
    }
}