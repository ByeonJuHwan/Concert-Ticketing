package org.ktor_lecture.concertservice.adapter.out.search

import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument
import org.ktor_lecture.concertservice.adapter.out.search.repository.ConcertSearchRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertDocumentRepository
import org.springframework.stereotype.Component

@Component
class ConcertDocumentRepositoryAdapter (
    private val concertSearchRepository: ConcertSearchRepository,
): ConcertDocumentRepository {

    override fun saveDocument(concertDocument: ConcertDocument) {
        concertSearchRepository.save(concertDocument)
    }
}