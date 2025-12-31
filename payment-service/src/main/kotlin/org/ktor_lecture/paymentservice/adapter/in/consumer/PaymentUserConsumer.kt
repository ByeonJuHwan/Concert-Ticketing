package org.ktor_lecture.paymentservice.adapter.`in`.consumer

import org.ktor_lecture.paymentservice.application.port.`in`.http.PaymentUserCreateUseCase
import org.ktor_lecture.paymentservice.common.JsonUtil
import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentUserConsumer (
    private val paymentUserCreateUseCase: PaymentUserCreateUseCase,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["user.create"],
        groupId = "payment-user-create-group",
        concurrency = "3",
    )
    fun userCreatedConsumer(eventString: String) {
        try {
            val event = JsonUtil.decodeFromJson<UserCreatedEvent>(eventString)
            paymentUserCreateUseCase.createUser(event)
        } catch (e: Exception) {
            log.error("이벤트 처리 실패 : {}", eventString, e)
        }
    }
}