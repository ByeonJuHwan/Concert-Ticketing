package org.ktor_lecture.paymentservice.adapter.`in`.consumer

import org.ktor_lecture.paymentservice.adapter.out.kafka.KafkaTopics
import org.ktor_lecture.paymentservice.application.port.`in`.http.PaymentUserCreateUseCase
import org.ktor_lecture.paymentservice.common.JsonUtil
import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

/**
 * Kafka concurrency 흐름
 *
 * 애플리케이션 시작
 *     ↓
 * @KafkaListener 감지
 *     ↓
 * ConcurrentMessageListenerContainer 생성
 *     ↓
 * doStart() 호출
 *     ↓
 * for (i = 0; i < concurrency; i++) {
 *     ↓
 *     KafkaMessageListenerContainer 생성 (i번째)
 *     ↓
 *     container.start()
 *     ↓
 *     ListenerConsumer(Thread) 생성
 *     ↓
 *     thread.start()
 *     ↓
 *     스레드 실행 → run() 메서드
 *     ↓
 *     while (isRunning()) {
 *         consumer.poll()  ← Kafka 메시지 폴링
 *         ↓
 *         invokeListener() ← @KafkaListener 메서드 호출
 *     }
 * }
 */

@Component
class PaymentUserConsumer (
    private val paymentUserCreateUseCase: PaymentUserCreateUseCase,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = [KafkaTopics.User.CREATED],
        groupId = "payment-user-create-group",
        concurrency = "3",
    )
    @RetryableTopic (
        backoff = Backoff(multiplier = 2.0),
        dltTopicSuffix = ".dlt"
    )
    fun userCreatedConsumer(eventString: String) {
        try {
            val event = JsonUtil.decodeFromJson<UserCreatedEvent>(eventString)
            paymentUserCreateUseCase.createUser(event)
        } catch (e: ConcertException) {
            log.error("ConcertException 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            log.error("이벤트 처리 실패 : {}", eventString, e)
            throw e
        }
    }
}