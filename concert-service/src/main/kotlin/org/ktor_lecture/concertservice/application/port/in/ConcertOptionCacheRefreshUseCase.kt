package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.domain.event.ConcertOptionChangeEvent

interface ConcertOptionCacheRefreshUseCase {
    fun refreshConcertOptionCache(event: ConcertOptionChangeEvent)
}