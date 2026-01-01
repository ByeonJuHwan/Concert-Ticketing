package org.ktor_lecture.concertservice.adapter.`in`.consumer

import org.ktor_lecture.concertservice.domain.event.UserCreatedEvent
import org.ktor_lecture.concertservice.application.port.`in`.ConcertUserCreateUseCase
import org.ktor_lecture.concertservice.common.JsonUtil
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class UserConsumer (
    private val concertUserCreateUseCase: ConcertUserCreateUseCase,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["user.create"],
        groupId = "reservation-user-create-group",
        concurrency = "3",
    )
    @RetryableTopic (
        backoff = Backoff(multiplier = 2.0),
        dltTopicSuffix = ".dlt"
    )
    fun userCreatedConsumer(eventString: String) {
        try {
            val event = JsonUtil.decodeFromJson<UserCreatedEvent>(eventString)
            concertUserCreateUseCase.createUser(event)
        } catch (e: ConcertException) {
            log.error("ConcertException 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            log.error("이벤트 처리 실패 : {}", eventString, e)
            throw e
        }
    }
}