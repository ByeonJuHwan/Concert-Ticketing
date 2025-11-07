package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.dto.ConcertDateInfo

interface SearchAvailableDatesUseCase {
    fun getAvailableDates(concertId: Long): List<ConcertDateInfo>
}