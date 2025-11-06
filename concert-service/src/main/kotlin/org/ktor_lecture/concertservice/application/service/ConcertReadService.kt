package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableDatesUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchAvailableSeatUseCase
import org.ktor_lecture.concertservice.application.port.`in`.SearchConcertUseCase
import org.ktor_lecture.concertservice.application.service.dto.ConcertDateInfo
import org.ktor_lecture.concertservice.application.service.dto.ConcertInfo
import org.ktor_lecture.concertservice.application.service.dto.ConcertSeatInfo
import org.springframework.stereotype.Service

@Service
class ConcertReadService (

) : SearchConcertUseCase, SearchAvailableDatesUseCase, SearchAvailableSeatUseCase {

    override fun getConcerts(): List<ConcertInfo> {
        TODO("Not yet implemented")
    }

    override fun getAvailableDates(concertId: Long): List<ConcertDateInfo> {
        TODO("Not yet implemented")
    }

    override fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatInfo> {
        TODO("Not yet implemented")
    }
}