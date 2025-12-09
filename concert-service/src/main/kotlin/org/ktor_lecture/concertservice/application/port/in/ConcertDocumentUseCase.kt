package org.ktor_lecture.concertservice.application.port.`in`

import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent

interface ConcertDocumentUseCase {
    fun saveDocument(event: ConcertCreatedEvent)
}