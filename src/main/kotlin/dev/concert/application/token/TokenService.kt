package dev.concert.application.token

import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.application.token.dto.TokenValidationResult
import dev.concert.domain.entity.UserEntity

interface TokenService {
    fun generateToken(userId: Long): String
    fun isTokenExpired(token: String): Boolean
    fun getToken(token: String): TokenResponseDto
    fun deleteToken(user : UserEntity)
    fun manageTokenStatus()
    fun isAvailableToken(token: String): Boolean
    fun manageExpiredTokens()
    fun validateToken(token: String): TokenValidationResult
}