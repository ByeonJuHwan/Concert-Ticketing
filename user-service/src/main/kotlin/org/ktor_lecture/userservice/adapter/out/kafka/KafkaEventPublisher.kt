package org.ktor_lecture.userservice.adapter.out.kafka

import org.ktor_lecture.userservice.application.port.out.EventPublisher
import org.ktor_lecture.userservice.application.port.out.OutBoxRepository
import org.ktor_lecture.userservice.common.JsonUtil
import org.ktor_lecture.userservice.domain.entity.OutboxStatus
import org.ktor_lecture.userservice.domain.event.DomainEvent
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
    override fun publish(event: DomainEvent) {
        kafkaTemplate.send(
            "user.create",
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
}