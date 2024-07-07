package dev.concert.application.token

import dev.concert.application.token.dto.TokenResponseDto
import org.springframework.stereotype.Service

@Service
class TokenFacade (
    private val tokenService: TokenService,
){
    fun generateToken(userId: Long): String {
        return tokenService.generateToken(userId)
    }

    fun isTokenAllowed(token: String): Boolean {
        return tokenService.isTokenAllowed(token)
    }

    fun getToken(token: String): TokenResponseDto {
        return tokenService.getToken(token)
    }
}