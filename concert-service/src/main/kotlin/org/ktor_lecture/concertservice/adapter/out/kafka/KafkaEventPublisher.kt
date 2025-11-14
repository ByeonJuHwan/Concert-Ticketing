package org.ktor_lecture.concertservice.adapter.out.kafka

import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.application.port.out.OutBoxRepository
import org.ktor_lecture.concertservice.common.JsonUtil
import org.ktor_lecture.concertservice.domain.entity.OutboxStatus
import org.ktor_lecture.concertservice.domain.event.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@Qualifier("kafka")
class KafkaEventPublisher (
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val outBoxRepository: OutBoxRepository,
) : EventPublisher {

    private val log = LoggerFactory.getLogger(KafkaEventPublisher::class.java)

    /**
     * [아웃박스 패턴]
     * 카프카 이벤트를 발행하고
     * 발행이 성공하면 SENT 로 변경
     * 발행이 실패하면 FAILED 로 변경
     */
    override fun publish(topic: String, event: DomainEvent) {
        kafkaTemplate.send(
            topic,
            event.eventId,
            JsonUtil.encodeToJson(event)
        ).whenComplete { _, exception ->
            if (exception == null) {
                outBoxRepository.updateStatus(event.eventId, OutboxStatus.SENT)
            } else {
                outBoxRepository.updateStatus(event.eventId, OutboxStatus.FAILED)
                log.error(exception.message)
            }
        }
    }

    override fun publish(event: DomainEvent) {
        log.info("이벤트 퍼블리싱")
    }

}