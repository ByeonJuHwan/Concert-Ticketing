package org.ktor_lecture.tokenservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TokenServiceApplication

fun main(args: Array<String>) {
    runApplication<TokenServiceApplication>(*args)
}
