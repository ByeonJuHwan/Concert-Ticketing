package org.ktor_lecture.concertservice.adapter.`in`.consumer

import org.apache.kafka.common.TopicPartition
import org.ktor_lecture.concertservice.adapter.out.kafka.KafkaTopics
import org.ktor_lecture.concertservice.domain.event.UserCreatedEvent
import org.ktor_lecture.concertservice.application.port.`in`.ConcertUserCreateUseCase
import org.ktor_lecture.concertservice.common.JsonUtil
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.cloud.context.environment.EnvironmentChangeEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.listener.AbstractConsumerSeekAware
import org.springframework.kafka.retrytopic.DltStrategy
import org.springframework.retry.annotation.Backoff
import org.springframework.stereotype.Component

@Component
class UserConsumer (
    private val concertUserCreateUseCase: ConcertUserCreateUseCase,
    private val environment: Environment,
): AbstractConsumerSeekAware() {

    companion object {
        private val SEEK_KEYS = setOf(
            "spring.kafka.seek.topic",
            "spring.kafka.seek.partition",
            "spring.kafka.seek.timestamp",
        )
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        id = "user-created-listener",
        topics = [KafkaTopics.User.CREATED],
        groupId = "reservation-user-create-group",
        concurrency = "1",
    )
    @RetryableTopic (
        backoff = Backoff(multiplier = 2.0),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
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

    @EventListener
    fun onSeekPartitionChange(event: EnvironmentChangeEvent) {
        if (event.keys.none { it in SEEK_KEYS}) return

        val seekPartition = environment.getProperty<Int>("spring.kafka.seek.partition") ?: return
        val seekTimestamp = environment.getProperty<Long>("spring.kafka.seek.timestamp") ?: return
        val seekTopic = environment.getProperty<String>("spring.kafka.seek.topic") ?: return

        seekPartitionByTimeStamp(seekTopic, seekPartition, seekTimestamp)
    }

    fun seekPartitionByTimeStamp(topic: String, partition: Int, timestamp: Long) {
        getSeekCallbacksFor(TopicPartition(topic, partition))
            ?.forEach { callback ->
                log.info("seek to timestamp — topic :{}, partition: {}, timestamp: {}", topic, partition, timestamp)
                callback.seekToTimestamp(topic, partition, timestamp)
            }
    }
}