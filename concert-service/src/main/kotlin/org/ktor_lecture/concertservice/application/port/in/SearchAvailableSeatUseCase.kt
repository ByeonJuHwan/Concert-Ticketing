package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.application.service.dto.ConcertSeatInfo

interface SearchAvailableSeatUseCase {
    fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatInfo>
}