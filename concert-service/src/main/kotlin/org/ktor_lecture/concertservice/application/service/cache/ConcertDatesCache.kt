package org.ktor_lecture.concertservice.application.service.cache

import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity

data class ConcertDatesCache(
    val dates: List<ConcertOptionEntity>
) {
    companion object {
        fun from(dates: List<ConcertOptionEntity>) = ConcertDatesCache(dates)
    }
}
