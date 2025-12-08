package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableDatesUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchConcertUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.service.dto.ConcertDateInfo
import org.ktor_lecture.concertservice.application.service.dto.ConcertInfo
import org.ktor_lecture.concertservice.application.service.dto.ConcertSeatInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ConcertReadService (
    private val concertReadRepository: ConcertReadRepository,
) : SearchConcertUseCase, SearchAvailableDatesUseCase, SearchAvailableSeatUseCase {

    @Transactional(readOnly = true)
    override fun getConcerts(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertInfo> {
        val concerts = concertReadRepository.getConcerts(concertName, singer, startDate, endDate)
        return concerts.map { ConcertInfo.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getAvailableDates(concertId: Long): List<ConcertDateInfo> {
        val concertsDates = concertReadRepository.getAvailableDates(concertId)
        return concertsDates.map { ConcertDateInfo.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatInfo> {
        val availableSeats = concertReadRepository.getAvailableSeats(concertOptionId)
        return availableSeats.map { ConcertSeatInfo.from(it) }
    }
}