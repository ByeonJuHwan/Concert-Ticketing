package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import java.util.Optional

interface ConcertReadRepository {
    fun getConcerts(): List<ConcertEntity>
    fun getAvailableDates(concertId: Long): List<ConcertOptionEntity>
    fun getAvailableSeats(concertOptionId: Long): List<SeatEntity>
    fun findUserById(userId: Long): Optional<ConcertUserEntity>
}