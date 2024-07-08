package dev.concert

import dev.concert.exception.NotFoundSeatException
import dev.concert.exception.SeatIsNotAvailableException
import dev.concert.exception.TokenNotFoundException
import dev.concert.exception.UserNotFountException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ErrorResponse(val code: String, val message: String)

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", e.message ?: "잘못된 요청입니다"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(UserNotFountException::class)
    fun handleUserNotFountException(e: UserNotFountException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("404", e.message ?: "존재하는 회원이 없습니다"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(TokenNotFoundException::class)
    fun handleTokenNotFoundException(e: TokenNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("401", e.message ?: "토큰이 존재하지 않습니다"),
            HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(NotFoundSeatException::class)
    fun handleNotFoundSeatException(e: NotFoundSeatException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("404", e.message ?: "존재하는 좌석이 없습니다"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(SeatIsNotAvailableException::class)
    fun handleSeatIsNotAvailableException(e: SeatIsNotAvailableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("SEAT_NOT_AVAILABLE", e.message ?: "예약 가능한 상태가 아닙니다"),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다"),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}