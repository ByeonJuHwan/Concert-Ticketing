package dev.concert.interfaces.consumer

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer {

    var lastReceivedMessage: String? = null

    @KafkaListener(topics = ["test_topic"], groupId = "test-group")
    fun consume(message: String) {
        lastReceivedMessage = message
    }
}