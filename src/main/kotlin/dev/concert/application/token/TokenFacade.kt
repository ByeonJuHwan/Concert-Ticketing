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
 
    fun isTokenExpired(token: String): Boolean { 
        return tokenService.isTokenExpired(token) 
    } 
 
    fun getToken(token: String): TokenResponseDto { 
        return tokenService.getToken(token) 
    } 
 
    fun manageTokenStatus() { 
        tokenService.manageTokenStatus() 
    } 

    fun isAvailableToken(token: String): Boolean { 
        return tokenService.isAvailableToken(token) 
    } 
}
