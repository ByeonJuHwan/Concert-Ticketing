package dev.concert.interfaces

import dev.concert.exception.NotEnoughPointException
import dev.concert.exception.NotFoundSeatException
import dev.concert.exception.ReservationAlreadyPaidException
import dev.concert.exception.ReservationExpiredException
import dev.concert.exception.ReservationNotFoundException
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
        logger.error("[IllegalArgumentException] [code : ${HttpStatus.BAD_REQUEST}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("400", e.message ?: "잘못된 요청입니다"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(UserNotFountException::class)
    fun handleUserNotFountException(e: UserNotFountException): ResponseEntity<ErrorResponse> {
        logger.error("[UserNotFountException] [code : ${HttpStatus.NOT_FOUND}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("404", e.message ?: "존재하는 회원이 없습니다"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(TokenNotFoundException::class)
    fun handleTokenNotFoundException(e: TokenNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error("[TokenNotFoundException] [code : ${HttpStatus.UNAUTHORIZED}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("401", e.message ?: "토큰이 존재하지 않습니다"),
            HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(NotFoundSeatException::class)
    fun handleNotFoundSeatException(e: NotFoundSeatException): ResponseEntity<ErrorResponse> {
        logger.error("[NotFoundSeatException] [code : ${HttpStatus.NOT_FOUND}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("404", e.message ?: "존재하는 좌석이 없습니다"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(SeatIsNotAvailableException::class)
    fun handleSeatIsNotAvailableException(e: SeatIsNotAvailableException): ResponseEntity<ErrorResponse> {
        logger.error("[SeatIsNotAvailableException] [code : ${HttpStatus.CONFLICT}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("409", e.message ?: "예약 가능한 상태가 아닙니다"),
            HttpStatus.CONFLICT
        )
    }
    @ExceptionHandler(ReservationNotFoundException::class)
    fun handleReservationNotFoundException(e: ReservationNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error("[ReservationNotFoundException] [code : ${HttpStatus.NOT_FOUND}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("404", e.message ?: "존재하는 예약이 없습니다"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(ReservationExpiredException::class)
    fun handleReservationExpiredException(e: ReservationExpiredException): ResponseEntity<ErrorResponse> {
        logger.error("[ReservationExpiredException] [code : ${HttpStatus.GONE}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("410", e.message ?: "예약이 만료되었습니다"),
            HttpStatus.GONE
        )
    }

    @ExceptionHandler(NotEnoughPointException::class)
    fun handleNotEnoughPointException(e: NotEnoughPointException): ResponseEntity<ErrorResponse> {
        logger.error("[NotEnoughPointException] [code : ${HttpStatus.BAD_REQUEST}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("400", e.message ?: "포인트가 부족합니다"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ReservationAlreadyPaidException::class)
    fun handleReservationAlreadyPaidException(e: ReservationAlreadyPaidException): ResponseEntity<ErrorResponse> {
        logger.error("[ReservationAlreadyPaidException] [code : ${HttpStatus.CONFLICT}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("409", e.message ?: "이미 결제된 예약입니다"),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("[Exception] [code : ${HttpStatus.INTERNAL_SERVER_ERROR}] [message : ${e.message}]")
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다"),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}