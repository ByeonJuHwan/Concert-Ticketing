package dev.concert.infrastructure.kafka.reservation

import dev.concert.domain.event.reservation.ReservationEvent
import dev.concert.domain.event.reservation.publisher.ReservationEventPublisher
import dev.concert.domain.repository.ReservationOutBoxRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@Qualifier("Kafka")
class ReservationKafkaProducer (
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val reservationOutBoxRepository: ReservationOutBoxRepository,
) : ReservationEventPublisher {

    private val log : Logger = LoggerFactory.getLogger(ReservationKafkaProducer::class.java)

    /**
     * [아웃박스 패턴]
     * 카프카 이벤트를 발행하고
     * 발행이 성공하면 SEND_SUCCESS 로 변경
     * 발행이 실패하면 SEND_FAIL 로 변경
     */
    override fun publish(event: ReservationEvent) {
        kafkaTemplate.send("reservation", event.toKafkaMessage())
            .whenComplete { _, exception ->
                if(exception == null) {
                    reservationOutBoxRepository.updateStatusSuccess(event.toEntity())
                    log.info("예약 Kafka Event 발행 성공!!")
                } else {
                    reservationOutBoxRepository.updateStatusFail(event.toEntity())
                    log.error("예약 Kafka Event 발행 실패!!", exception)
                }
            }
    }
}