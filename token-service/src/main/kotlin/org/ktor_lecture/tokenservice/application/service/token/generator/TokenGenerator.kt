package org.ktor_lecture.tokenservice.application.service.token.generator

interface TokenGenerator {
    fun generateToken(user: Long): String
}