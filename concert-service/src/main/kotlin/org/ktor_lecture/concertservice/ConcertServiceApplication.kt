package org.ktor_lecture.concertservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConcertServiceApplication

fun main(args: Array<String>) {
    runApplication<ConcertServiceApplication>(*args)
}
