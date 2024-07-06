package dev.concert.application.token

interface TokenService {
    fun generateToken(userId: Long): String
}