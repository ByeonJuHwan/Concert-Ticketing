package org.ktor_lecture.concertservice.adapter.`in`.event

import org.ktor_lecture.concertservice.application.port.`in`.ConcertDocumentUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ConcertOptionCacheRefreshUseCase
import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent
import org.ktor_lecture.concertservice.domain.event.ConcertOptionChangeEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ConcertEventListener (
    private val concertDocumentUseCase: ConcertDocumentUseCase,
    private val concertOptionCacheRefreshUseCase: ConcertOptionCacheRefreshUseCase,
) {

    /**
     * 콘서트 정보가 DB에 저장되면 ElasticSearch 에 Index 저장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleConcertCreatedEvent(event: ConcertCreatedEvent) {
        concertDocumentUseCase.saveDocument(event)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleConcertOptionChangeEvent(event: ConcertOptionChangeEvent) {
        concertOptionCacheRefreshUseCase.refreshConcertOptionCache(event)
    }
}