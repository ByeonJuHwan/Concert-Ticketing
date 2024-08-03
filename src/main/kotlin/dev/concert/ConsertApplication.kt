package dev.concert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@EnableCaching
@EnableScheduling
@SpringBootApplication
class ConsertApplication

fun main(args: Array<String>) {
	runApplication<ConsertApplication>(*args)
}
