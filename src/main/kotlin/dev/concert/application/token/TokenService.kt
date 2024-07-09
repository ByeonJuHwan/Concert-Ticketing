package dev.concert.application.token

import dev.concert.application.token.dto.TokenResponseDto

interface TokenService {
    fun generateToken(userId: Long): String
    fun isTokenAllowed(token: String): Boolean
    fun getToken(token: String): TokenResponseDto
}