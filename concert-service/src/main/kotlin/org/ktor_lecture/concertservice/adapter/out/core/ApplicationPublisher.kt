package org.ktor_lecture.concertservice.adapter.out.core

import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.domain.event.DomainEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
@Qualifier("application")
class ApplicationPublisher(
    private val eventPublisher: ApplicationEventPublisher
): EventPublisher {

    private val log : Logger = LoggerFactory.getLogger(ApplicationPublisher::class.java)

    override fun publish(event: DomainEvent) {
        eventPublisher.publishEvent(event)
        log.info("Concert-Service 내부 이벤트 발행 : {}", event.eventId)
    }

    override fun publish(topic: String, event: DomainEvent) {
        TODO("Not yet implemented")
    }
}