package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.concertservice.IntegrationTestBase
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.ConcertWriteService
import org.ktor_lecture.concertservice.application.service.command.CreateConcertCommand
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.domain.event.DomainEvent
import org.ktor_lecture.concertservice.fixture.ConcertFixtures
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.web.servlet.get
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@RecordApplicationEvents
class ConcertEventTest : IntegrationTestBase() {

    @Autowired
    private lateinit var concertWriteRepository: ConcertWriteRepository

    @Autowired
    private lateinit var concertWriteService: ConcertWriteService

    @Autowired
    private lateinit var applicationEvents: ApplicationEvents

    @AfterEach
    fun tearDown() {
        concertWriteRepository.deleteAll()
    }

    @Test
    fun `콘서트를 생성하면 내부 이벤트가 발행된다`() {
        val command = CreateConcertCommand(
            concertName = "아이유 콘서트",
            singer = "아이유",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now()
        )

        // when
        concertWriteService.createConcert(command)

        // then
        val events = applicationEvents.stream(DomainEvent::class.java)

        assertThat(events).hasSize(1)
    }
}