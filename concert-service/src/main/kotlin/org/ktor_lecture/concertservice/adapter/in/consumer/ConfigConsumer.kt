package org.ktor_lecture.concertservice.adapter.`in`.consumer

import org.slf4j.LoggerFactory
import org.springframework.cloud.context.environment.EnvironmentChangeEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class ConfigConsumer (
    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry,
    private val environment: Environment,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @EventListener
    fun onConcurrencyChange(event: EnvironmentChangeEvent) {
        if ("spring.kafka.listener.concurrency" !in event.keys) return

        val newConcurrency = environment.getProperty(
            "spring.kafka.listener.concurrency", Int::class.java, 1
        )
        val container = kafkaListenerEndpointRegistry
            .getListenerContainer("user-created-listener")
                as? ConcurrentMessageListenerContainer<*, *>
            ?: return

        container.stop()
        container.concurrency = newConcurrency
        container.start()
        log.info("Kafka concurrency 변경 완료: {}", newConcurrency)
    }
}