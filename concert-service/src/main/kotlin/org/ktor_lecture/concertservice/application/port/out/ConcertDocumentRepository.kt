package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument

interface ConcertDocumentRepository {
    fun saveDocument(concertDocument: ConcertDocument)
}