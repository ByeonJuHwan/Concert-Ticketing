package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.ktor_lecture.concertservice.adapter.`in`.consumer.UserConsumer
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/seek")
class PartitionSeekController (
    private val userConsumer: UserConsumer,
) {

    @PostMapping("/timestamp")
    fun seekToTimestamp(
        @RequestParam timestamp: Long,
        @RequestParam partition: Int,
        @RequestParam topic: String,
    ): ResponseEntity<String> {
        userConsumer.seekPartitionByTimeStamp(topic, partition, timestamp)
        return ResponseEntity.ok("seek 명령 등록 완료 — 다음 poll 사이클부터 적용됩니다")
    }
}