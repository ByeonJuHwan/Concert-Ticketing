package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.dto.ConcertInfo

interface SearchConcertUseCase {
    fun getConcerts(): List<ConcertInfo>
}