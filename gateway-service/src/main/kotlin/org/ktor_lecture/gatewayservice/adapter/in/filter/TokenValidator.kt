package org.ktor_lecture.gatewayservice.adapter.`in`.filter

import kotlinx.serialization.Serializable
import org.ktor_lecture.gatewayservice.application.port.out.TokenRepository
import org.ktor_lecture.gatewayservice.common.JsonUtil
import org.ktor_lecture.gatewayservice.domain.status.TokenValidationResult
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class TokenValidator (
    private val tokenRepository: TokenRepository,
): AbstractGatewayFilterFactory<TokenValidator.Config>(Config::class.java) {


    /**
     * 대기열 토큰 검증
     *
     * 1. HttpHeader 에 대기열 토큰이 존재하는지 검사 -> 없으면 Error
     * 2. 토큰이 ACTIVE_QUEUE 에 존재하는지 검사 -> 없으면 Error
     */
    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader.isNullOrEmpty() || !authHeader.startsWith("Bearer ")) {
                return@GatewayFilter writeErrorResponse(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Authorization 헤더가 없거나 형식이 올바르지 않습니다"
                )
            }

            val token = authHeader.substring(7)
            val validateResult = tokenRepository.validateToken(token)

            if (validateResult == TokenValidationResult.NOT_AVAILABLE) {
                return@GatewayFilter writeErrorResponse(
                    exchange,
                    HttpStatus.BAD_REQUEST,
                    "유효하지 않은 대기열 토큰입니다"
                )
            }

            chain.filter(exchange)
        }
    }

    private fun writeErrorResponse(
        exchange: ServerWebExchange,
        status: HttpStatus,
        message: String
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON

        val errorBody = ErrorBody(
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            timestamp = LocalDateTime.now().toString(),
        )

        val bytes = JsonUtil.encodeToJson(errorBody)
        val buffer = response.bufferFactory().wrap(bytes.toByteArray())

        return response.writeWith(Mono.just(buffer))
    }

    class Config
}

@Serializable
data class ErrorBody (
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: String,
)