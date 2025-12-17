package org.ktor_lecture.concertservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.application.port.out.OutBoxRepository
import org.ktor_lecture.concertservice.domain.entity.OutBox
import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent

@ExtendWith(MockKExtension::class)
class ReservationEventServiceTest {


    @MockK
    private lateinit var outBoxRepository: OutBoxRepository

    @MockK
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var reservationEventService: ReservationEventService

    @Test
    fun `좌석 예약 이벤트 아웃박스 메세지 저장`() {
        val event = ReservationCreatedEvent(
            reservationId = 1L
        )
        
        every { outBoxRepository.save(any(OutBox::class))} just runs
        
        // when
        reservationEventService.handleReservationCreatedOutBox(event)
        
        // then
        verify(exactly = 1) {outBoxRepository.save(any(OutBox::class))}
    }
}