package dev.concert.presentation.filter

import dev.concert.application.token.TokenFacade
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component 
class TokenValidateFilter ( 
    private val tokenFacade: TokenFacade, 
) : OncePerRequestFilter() { 
 
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) { 
        val token = getBearerToken(request) 
        if (token == null) { 
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "토큰이 없습니다") 
            return 
        }else if (tokenFacade.isTokenExpired(token)) { 
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "토큰이 만료되었습니다") 
            return 
        } else if(!tokenFacade.isAvailableToken(token)){ 
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "대기열 순번 확인이 필요합니다") 
            return 
        } 
        filterChain.doFilter(request, response) 
    } 
 
    private fun getBearerToken(request: HttpServletRequest): String? { 
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) 
        return if (header != null && header.startsWith("Bearer ")) { 
            header.substring(7) 
        } else { 
            null 
        } 
    } 
} 
