package dev.concert.presentation.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

class LoggingFilter : OncePerRequestFilter() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cachingRequest = ContentCachingRequestWrapper(request)
        val cachingResponse = ContentCachingResponseWrapper(response)

        logger.info(requestLog(cachingRequest))
        filterChain.doFilter(cachingRequest, cachingResponse)
        logger.info(responseLog(cachingResponse))

        cachingResponse.copyBodyToResponse();
    }

    private fun requestLog(request: ContentCachingRequestWrapper): String {
        val requestBody = String(request.contentAsByteArray)
        return """
            
            HTTP Request:
            [${request.method}] ${request.requestURI}
            Headers: ${request.headerNames.toList().joinToString { header -> "$header: ${request.getHeader(header)}" }}
            Body: $requestBody
        """.trimIndent()
    }

    private fun responseLog(response: ContentCachingResponseWrapper): String {
        val responseBody = String(response.contentAsByteArray)
        return """
            
            HTTP Response:
            [${response.status}]
            Headers: ${response.headerNames.joinToString { header -> "$header: ${response.getHeader(header)}" }}
            Body: $responseBody
        """.trimIndent()
    }
}