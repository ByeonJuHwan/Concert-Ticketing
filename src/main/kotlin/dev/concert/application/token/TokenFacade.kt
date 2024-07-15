package dev.concert.application.token

import dev.concert.application.token.dto.TokenResponseDto
import dev.concert.application.token.dto.TokenValidationResult
import org.springframework.stereotype.Service

@Service 
class TokenFacade ( 
    private val tokenService: TokenService, 
){ 
    fun generateToken(userId: Long): String { 
        return tokenService.generateToken(userId) 
    }
 
    fun getToken(token: String): TokenResponseDto { 
        return tokenService.getToken(token) 
    } 
 
    fun manageTokenStatus() { 
        tokenService.manageTokenStatus() 
    }

    fun validateToken(token: String): TokenValidationResult {
        return tokenService.validateToken(token)
    }
}
