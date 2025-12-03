package org.ktor_lecture.gatewayservice.adapter.`in`.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class LoggingFilter : GlobalFilter, Ordered {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return if (exchange.request.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) {
            logRequestBody(exchange, chain)
        } else {
            logRequest(exchange)
            chain.filter(exchange)
        }
    }

    private fun logRequestBody(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return DataBufferUtils.join(exchange.request.body)
            .defaultIfEmpty(exchange.response.bufferFactory().wrap(ByteArray(0)))
            .flatMap { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)

                val body = String(bytes, Charsets.UTF_8)
                logRequest(exchange, body)

                // Body를 다시 생성
                val cachedBody = exchange.response.bufferFactory().wrap(bytes)
                val decorator = object : ServerHttpRequestDecorator(exchange.request) {
                    override fun getBody(): Flux<DataBuffer> = Flux.just(cachedBody)
                }

                chain.filter(exchange.mutate().request(decorator).build())
            }
    }

    private fun logRequest(exchange: ServerWebExchange, body: String = "") {
        val request = exchange.request
        logger.info(
            """
            ===== HTTP Request =====
            [${request.method}] ${request.uri.path}
            Headers: ${request.headers.entries.take(5).joinToString { "${it.key}: ${it.value}" }}
            Body: ${body.take(500)}
            """.trimIndent()
        )
    }

    override fun getOrder() = Ordered.HIGHEST_PRECEDENCE
}