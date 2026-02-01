package org.ktor_lecture.tokenservice.adapter.`in`.cosumer

import org.ktor_lecture.tokenservice.adapter.out.kafka.KafkaTopics
import org.ktor_lecture.tokenservice.application.port.`in`.QueueTokenUserCreateUseCase
import org.ktor_lecture.tokenservice.common.JsonUtil
import org.ktor_lecture.tokenservice.domain.event.UserCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class QueueTokenUserConsumer (
    private val queueTokenUserCreateUseCase: QueueTokenUserCreateUseCase,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = [KafkaTopics.User.CREATED],
        groupId = "token-user-create-group",
        concurrency = "3",
    )
    fun userCreatedConsumer(eventString: String) {
        try {
            val event = JsonUtil.decodeFromJson<UserCreatedEvent>(eventString)
            queueTokenUserCreateUseCase.createTokenUser(event)
        } catch (e: Exception) {
            log.error("Queue_Token_User 이벤트 처리 실패 : {}", eventString, e)
        }
    }
}