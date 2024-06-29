package dev.consert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConsertApplication

fun main(args: Array<String>) {
	runApplication<ConsertApplication>(*args)
}
