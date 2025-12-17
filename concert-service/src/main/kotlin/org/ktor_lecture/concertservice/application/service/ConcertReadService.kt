package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.GetConcertSuggestionUseCase
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
) : SearchConcertUseCase, SearchAvailableDatesUseCase, SearchAvailableSeatUseCase, GetConcertSuggestionUseCase {

    /**
     * 콘서트를 조회한다
     *
     * 1. ElasticSearch 에서 조회
     * 2. 예외 발생시 DB Like 문 조횐
     */
    @Transactional(readOnly = true)
    override fun getConcerts(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertInfo> {
        val concerts = concertReadRepository.getConcerts(concertName, singer, startDate, endDate)
        return concerts.map { ConcertInfo.from(it) }
    }

    /**
     * 선택한 콘서트의 구체적인 정보를 조회한다
     * ex) 콘서트 장소, 콘서트 시작 날짜, 콘서트 종료 날짜...
     */
    @Transactional(readOnly = true)
    override fun getAvailableDates(concertId: Long): List<ConcertDateInfo> {
        val concertsDates = concertReadRepository.getAvailableDates(concertId)
        return concertsDates.map { ConcertDateInfo.from(it) }
    }

    /**
     * 선택한 콘서트 날짜의 좌석 정보를 조회한다
     */
    @Transactional(readOnly = true)
    override fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatInfo> {
        val availableSeats = concertReadRepository.getAvailableSeats(concertOptionId)
        return availableSeats.map { ConcertSeatInfo.from(it) }
    }

    /**
     * 콘서트 검색어 자동완성 추천
     */
    override fun getConcertSuggestions(query: String): List<String> {
        return concertReadRepository.getConcertSuggestions(query)
    }
}