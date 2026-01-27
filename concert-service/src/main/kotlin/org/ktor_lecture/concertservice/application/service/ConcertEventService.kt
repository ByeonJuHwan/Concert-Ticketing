package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.adapter.out.kafka.KafkaTopics
import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument
import org.ktor_lecture.concertservice.application.port.`in`.ConcertDocumentUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ConcertOptionCacheRefreshUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertDocumentRepository
import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent
import org.ktor_lecture.concertservice.domain.event.ConcertOptionChangeEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ConcertEventService (
    private val concertDocumentRepository: ConcertDocumentRepository,
    @Qualifier("kafka") private val eventPublisher: EventPublisher,
): ConcertDocumentUseCase, ConcertOptionCacheRefreshUseCase {

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

    /**
     * 콘서트 옵션 변경에 따른 캐시 갱신 -> Kafka 이벤트 발행
     */
    override fun refreshConcertOptionCache(event: ConcertOptionChangeEvent) {
        eventPublisher.publish(KafkaTopics.Concert.OPTION_CHANGED, event)
    }
}