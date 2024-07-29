package dev.concert.application.token

import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.application.token.dto.TokenValidationResult
import dev.concert.domain.service.user.UserService
import dev.concert.domain.service.token.TokenService
import org.springframework.stereotype.Service

@Service
class TokenFacade (
    private val tokenService: TokenService,
    private val userService: UserService,
){
    fun generateToken(userId: Long): String {
        val user = userService.getUser(userId)
        return tokenService.generateToken(user)
    }

    fun getToken(userId: Long): TokenResponseDto {
        val user = userService.getUser(userId)
        return tokenService.getToken(user)
    }

    fun manageTokenStatus() {
        tokenService.manageTokenStatus()
    }

    fun manageExpiredTokens() {
        tokenService.manageExpiredTokens()
    }

    fun validateToken(token: String): TokenValidationResult {
        return tokenService.validateToken(token)
    }
}