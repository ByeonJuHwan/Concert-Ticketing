package org.ktor_lecture.userservice.application.service

import org.ktor_lecture.userservice.application.port.`in`.CreateUserCreateOutBoxUseCase
import org.ktor_lecture.userservice.application.port.`in`.SendUserCreatedEventUseCase
import org.ktor_lecture.userservice.application.port.out.EventPublisher
import org.ktor_lecture.userservice.application.port.out.OutBoxRepository
import org.ktor_lecture.userservice.common.JsonUtil
import org.ktor_lecture.userservice.domain.entity.OutBox
import org.ktor_lecture.userservice.domain.event.UserCreatedEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserEventService(
    private val outBoxRepository: OutBoxRepository,
    @Qualifier("kafka") private val eventPublisher: EventPublisher,
): CreateUserCreateOutBoxUseCase, SendUserCreatedEventUseCase {

    @Transactional
    override fun recordUserCreatedOutBoxMsg(event: UserCreatedEvent) {
        val outBox = OutBox(
            aggregateType = "UserEntity",
            eventId = event.eventId,
            aggregateId = event.userId,
            eventType = "UserCreatedEvent",
            payload = JsonUtil.encodeToJson(event)
        )

        outBoxRepository.save(outBox)
    }


    override fun publishUseCreatedEvent(event: UserCreatedEvent) {
        eventPublisher.publish(event)
    }
}
