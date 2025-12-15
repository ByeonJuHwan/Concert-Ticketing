package org.ktor_lecture.userservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.userservice.application.port.out.EventPublisher
import org.ktor_lecture.userservice.application.port.out.OutBoxRepository
import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.ktor_lecture.userservice.domain.event.UserCreatedEvent

@ExtendWith(MockKExtension::class)
class UserEventServiceTest {

    @MockK
    private lateinit var outBoxRepository: OutBoxRepository

    @MockK
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var userEventService: UserEventService

    @Test
    fun `recordUserCreatedOutBoxMsg_아웃박스 메세지 저장`() {
        val event = UserCreatedEvent(
            userId = "userId",
            userName = "test",
        )

        every { outBoxRepository.save(any()) } just runs

        userEventService.recordUserCreatedOutBoxMsg(event)

        verify(exactly = 1) {outBoxRepository.save(any())}
    }

    @Test
    fun `publishUseCreatedEvent_유저 생성 이벤트 발행`() {
        val event = UserCreatedEvent(
            userId = "userId",
            userName = "test",
        )

        every { eventPublisher.publish(event) } just runs

        userEventService.publishUseCreatedEvent(event)

        verify(exactly = 1) {eventPublisher.publish(event)}
    }

    @Test
    fun `userCreatedEventRetryScheduler_유저 생성 이벤트 재시도 스케줄러`() {
        val outBox1 = OutBox(
            aggregateType = "UserEntity",
            eventId = "test_eventId",
            aggregateId = "test_aggregateId",
            eventType = "UserCreatedEvent",
            payload = """{"userId":"1","userName":"test1"}"""
        )
        val outBox2 = OutBox(
            aggregateType = "UserEntity",
            eventId = "test_eventId",
            aggregateId = "test_aggregateId",
            eventType = "UserCreatedEvent",
            payload = """{"userId":"2","userName":"test2"}"""
        )

        val outBoxes = listOf(outBox1, outBox2)

        // given
        every { outBoxRepository.getFailedEvents() } returns outBoxes
        every { outBoxRepository.increaseRetryCount(any(String::class)) } just runs
        every { eventPublisher.publish(any()) } just runs

        // when
        userEventService.userCreatedEventRetryScheduler()

        // when
        verify(exactly = 2) {outBoxRepository.increaseRetryCount(any(String::class))}
        verify(exactly = 2) { eventPublisher.publish(any())}
    }

    @Test
    fun `userCreatedEventRetryScheduler_유저 생성 이벤트 재시도 카운트 초과 실패`() {
        val outBox1 = OutBox(
            aggregateType = "UserEntity",
            eventId = "test_eventId",
            aggregateId = "test_aggregateId",
            eventType = "UserCreatedEvent",
            payload = """{"userId":"1","userName":"test1"}"""
        )
        val outBox2 = OutBox(
            aggregateType = "UserEntity",
            eventId = "test_eventId",
            aggregateId = "test_aggregateId",
            eventType = "UserCreatedEvent",
            payload = """{"userId":"2","userName":"test2"}""",
            retryCount = 3
        )

        val outBoxes = listOf(outBox1, outBox2)

        every { outBoxRepository.getFailedEvents() } returns outBoxes
        every { outBoxRepository.updateStatus(any(String::class), any(OutboxStatus::class)) } just runs
        every { outBoxRepository.increaseRetryCount(any(String::class)) } just runs
        every { eventPublisher.publish(any()) } just runs

        // when
        userEventService.userCreatedEventRetryScheduler()

        // then
        verify(exactly = 1) {outBoxRepository.increaseRetryCount(any(String::class))}
        verify(exactly = 1) { eventPublisher.publish(any())}
    }

}