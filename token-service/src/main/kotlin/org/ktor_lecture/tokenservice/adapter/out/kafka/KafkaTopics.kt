package org.ktor_lecture.tokenservice.adapter.out.kafka

object KafkaTopics {

    private const val USER_PREFIX = "user"

    object User {
        const val CREATED = "$USER_PREFIX.created"
    }
}