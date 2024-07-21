package dev.concert.domain.service.token

import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.application.token.dto.TokenValidationResult
import dev.concert.domain.entity.UserEntity

interface TokenService {
    fun generateToken(user:UserEntity): String
    fun getToken(token: String): TokenResponseDto
    fun deleteToken(user : UserEntity)
    fun manageTokenStatus()
    fun manageExpiredTokens()
    fun validateToken(token: String): TokenValidationResult
}