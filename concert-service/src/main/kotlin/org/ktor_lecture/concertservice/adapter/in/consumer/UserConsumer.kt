package org.ktor_lecture.concertservice.adapter.`in`.consumer

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class UserConsumer {

    @KafkaListener(topics = ["user.create"], groupId = "user-create-group")
    fun userCreatedConsumer() {
        println("컴슘성공")
    }
}