package dev.concert.application.token.dto

enum class TokenValidationResult {
    VALID,
    INVALID,
    EXPIRED,
    NOT_AVAILABLE
}