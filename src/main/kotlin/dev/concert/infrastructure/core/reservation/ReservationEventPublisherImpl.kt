package dev.concert.infrastructure.core.reservation

import dev.concert.domain.event.reservation.ReservationEvent
import dev.concert.domain.event.reservation.publisher.ReservationEventPublisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ReservationEventPublisherImpl (
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ReservationEventPublisher {

    private val log : Logger = LoggerFactory.getLogger(ReservationEventPublisherImpl::class.java)

    override fun publish(event: ReservationEvent) {
        applicationEventPublisher.publishEvent(event)
        log.info("예약 이벤트 발행 성공")
    }
}