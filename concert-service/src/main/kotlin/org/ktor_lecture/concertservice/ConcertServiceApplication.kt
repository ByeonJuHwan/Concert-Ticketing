package org.ktor_lecture.concertservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

//@EnableScheduling
@SpringBootApplication
class ConcertServiceApplication

fun main(args: Array<String>) {
    runApplication<ConcertServiceApplication>(*args)
}
