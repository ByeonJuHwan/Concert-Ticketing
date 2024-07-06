package dev.concert.application.token

import org.springframework.stereotype.Service

@Service
class TokenFacade (
    private val tokenService: TokenService,
){
    fun generateToken(userId: Long): String {
        return tokenService.generateToken(userId)
    }
}