package dev.concert.infrastructure.kafka.reservation

import dev.concert.domain.event.reservation.ReservationEvent
import dev.concert.domain.event.reservation.publisher.ReservationEventPublisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@Qualifier("Kafka")
class ReservationKafkaProducer (
    private val kafkaTemplate: KafkaTemplate<String, String>
) : ReservationEventPublisher {

    private val log : Logger = LoggerFactory.getLogger(ReservationKafkaProducer::class.java)

    override fun publish(event: ReservationEvent) {
        kafkaTemplate.send("reservation", event.toKafkaMessage())
        log.info("예약 Kafka Event 발행!!")
    }
}