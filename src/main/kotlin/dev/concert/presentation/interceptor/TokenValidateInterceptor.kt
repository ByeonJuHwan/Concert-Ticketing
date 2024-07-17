package dev.concert.presentation.interceptor

import dev.concert.application.token.TokenFacade
import dev.concert.application.token.dto.TokenValidationResult
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class TokenValidateInterceptor (
    private val tokenFacade: TokenFacade,
) : HandlerInterceptor {

    private val logger: Logger = LoggerFactory.getLogger(TokenValidateInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token = getBearerToken(request) ?: run {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "토큰이 없습니다")
            return false
        }

        val validateResult = tokenFacade.validateToken(token)

        return when (validateResult) {
            TokenValidationResult.EXPIRED -> {
                logger.warn("만료된 토큰입니다 : $token")
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "토큰이 만료되었습니다")
                false
            }
            TokenValidationResult.NOT_AVAILABLE -> {
                logger.warn("대기열 순번 확인이 필요한 토큰입니다 : $token")
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "대기열 순번 확인이 필요합니다")
                false
            }
            TokenValidationResult.INVALID -> {
                logger.warn("유효하지 않은 토큰입니다 : $token")
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다")
                false
            }
            TokenValidationResult.VALID -> true
        }
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