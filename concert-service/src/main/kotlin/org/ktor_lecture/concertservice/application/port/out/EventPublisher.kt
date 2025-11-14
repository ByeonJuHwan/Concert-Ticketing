package org.ktor_lecture.concertservice.application.port.out

import org.ktor_lecture.concertservice.domain.event.DomainEvent


interface EventPublisher {

    fun publish(event: DomainEvent)

    fun publish(topic: String, event: DomainEvent)
}