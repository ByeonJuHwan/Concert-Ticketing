package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.domain.event.DomainEvent

interface EventPublisher {

    fun publish(event: DomainEvent)
}