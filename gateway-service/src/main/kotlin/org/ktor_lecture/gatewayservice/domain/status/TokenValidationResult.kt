package org.ktor_lecture.gatewayservice.domain.status

enum class TokenValidationResult {
    VALID,
    INVALID,
    EXPIRED,
    NOT_AVAILABLE
}