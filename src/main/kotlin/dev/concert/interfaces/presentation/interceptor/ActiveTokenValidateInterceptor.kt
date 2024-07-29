package dev.concert.interfaces.presentation.interceptor

import dev.concert.domain.service.token.ACTIVE_QUEUE
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ActiveTokenValidateInterceptor (
    private val redisTemplate: RedisTemplate<String, String>
) : HandlerInterceptor {

    private val log : Logger = LoggerFactory.getLogger(ActiveTokenValidateInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token = getBearerToken(request) ?: run {
            response.sendError(HttpStatus.NOT_FOUND.value(), "토큰이 없습니다")
            return false
        }

        val isActive = redisTemplate.opsForSet().isMember(ACTIVE_QUEUE, token) ?: throw IllegalArgumentException("유효한 토큰이 아닙니다 : $token")
        if(!isActive) {
            log.warn("active queue 에 토큰이 존재하지 않습니다 : $token")
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효한 토큰이 아닙니다")
            return false
        }

        return true
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