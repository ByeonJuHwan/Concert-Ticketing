package org.ktor_lecture.concertservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.concertservice.application.port.out.ConcertDocumentRepository
import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent
import java.time.LocalDate


@ExtendWith(MockKExtension::class)
class ConcertEventServiceTest {

    @MockK
    private lateinit var concertDocumentRepository: ConcertDocumentRepository

    @InjectMockKs
    private lateinit var concertEventService: ConcertEventService

    @Test
    fun `saveDocument_콘서트가 생성되면 ES Document 생성된다`() {
        // given
        val event = ConcertCreatedEvent(
            id = "test_id",
            concertName = "test_ConcertNAme",
            singer = "test_singer",
            startDate = LocalDate.now().toString(),
            endDate = LocalDate.now().toString(),
            reserveStartDate = LocalDate.now().toString(),
            reserveEndDate = LocalDate.now().toString(),
        )

        every { concertDocumentRepository.saveDocument(any()) } just runs

        // when
        concertEventService.saveDocument(event)

        // then
        verify(exactly = 1) {concertDocumentRepository.saveDocument(any())}
    }

}