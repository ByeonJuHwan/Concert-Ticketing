package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.dto.ConcertInfo
import java.time.LocalDate

interface SearchConcertUseCase {
    fun getConcerts(concertName: String?, singer: String?, startDate: LocalDate?, endDate: LocalDate?): List<ConcertInfo>
}