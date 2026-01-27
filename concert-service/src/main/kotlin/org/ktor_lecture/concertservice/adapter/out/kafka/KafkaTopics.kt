package org.ktor_lecture.concertservice.adapter.out.kafka

object KafkaTopics {

    private const val USER_PREFIX = "user"
    private const val CONCERT_PREFIX = "concert"

    object User {
        const val CREATED = "$USER_PREFIX.created"
    }

    object Concert {
        const val OPTION_CHANGED = "$CONCERT_PREFIX.option.changed"
    }
}