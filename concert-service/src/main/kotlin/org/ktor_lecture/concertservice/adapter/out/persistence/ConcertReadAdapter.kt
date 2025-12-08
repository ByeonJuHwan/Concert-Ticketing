package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertOptionJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertSeatJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertUserJpaRepository
import org.ktor_lecture.concertservice.adapter.out.search.repository.ConcertSearchRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.Optional

@Component
class ConcertReadAdapter (
    private val concertJpaRepository: ConcertJpaRepository,
    private val concertOptionJpaRepository: ConcertOptionJpaRepository,
    private val concertSeatJpaRepository: ConcertSeatJpaRepository,
    private val concertUserJpaRepository: ConcertUserJpaRepository,
    private val concertSearchRepository: ConcertSearchRepository,
): ConcertReadRepository {
    override fun getConcerts(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertEntity> {
        try {
            val documents = concertSearchRepository.searchByOptions(concertName, singer, startDate?.toString(), endDate?.toString())
            return documents
                .map { d ->
                    ConcertEntity(
                        id = d.id.toLong(),
                        concertName = d.concertName,
                        singer = d.singer,
                        startDate = d.startDate,
                        endDate = d.endDate,
                        reserveStartDate = d.reserveStartDate,
                        reserveEndDate = d.reserveEndDate,
                    )
                }
        } catch (_: Exception) {
            return searchWithJpa(concertName, singer, startDate, endDate)
        }
    }

    override fun getAvailableDates(concertId: Long): List<ConcertOptionEntity> {
        return concertOptionJpaRepository.findAvailableDates(concertId)
    }

    override fun getAvailableSeats(concertOptionId: Long): List<SeatEntity> {
        return concertSeatJpaRepository.findByConcertOptionId(concertOptionId)
    }

    override fun findUserById(userId: Long): Optional<ConcertUserEntity> {
        return concertUserJpaRepository.findById(userId)
    }

    override fun getConcertSuggestions(query: String): List<String> {
        return concertSearchRepository.getSuggestions(query)
    }

    private fun searchWithJpa(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertEntity> {
        try {
            return concertJpaRepository.findConcertsByOptions(concertName, singer, startDate, endDate)
        } catch (_: Exception) {
            throw ConcertException(ErrorCode.CONCERT_SEARCH_ERROR)
        }
    }
}