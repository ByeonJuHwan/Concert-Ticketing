package dev.concert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ConsertApplication

fun main(args: Array<String>) {
	runApplication<ConsertApplication>(*args)
}
