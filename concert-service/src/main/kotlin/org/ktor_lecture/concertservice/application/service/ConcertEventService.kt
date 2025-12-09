package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument
import org.ktor_lecture.concertservice.application.port.`in`.ConcertDocumentUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertDocumentRepository
import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ConcertEventService (
    private val concertDocumentRepository: ConcertDocumentRepository,
): ConcertDocumentUseCase {

    override fun saveDocument(event: ConcertCreatedEvent) {
        val concertDocument = ConcertDocument(
            id = event.id,
            concertName = event.concertName,
            singer = event.singer,
            startDate = LocalDate.parse(event.startDate),
            endDate = LocalDate.parse(event.endDate),
            reserveStartDate = LocalDate.parse(event.reserveStartDate),
            reserveEndDate = LocalDate.parse(event.reserveEndDate),
        )

        concertDocumentRepository.saveDocument(concertDocument)
    }
}