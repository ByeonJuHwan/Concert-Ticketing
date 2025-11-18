package org.ktor_lecture.gatewayservice.application.port.out

import org.ktor_lecture.gatewayservice.domain.status.TokenValidationResult

interface TokenRepository {
    fun validateToken(token: String): TokenValidationResult
}