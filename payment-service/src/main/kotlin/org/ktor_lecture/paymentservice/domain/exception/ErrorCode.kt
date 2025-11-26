package org.ktor_lecture.paymentservice.domain.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val code: String, val message: String, val status: HttpStatus) {
    USER_NOT_FOUND("404", "존재하는 회원이 없습니다", HttpStatus.NOT_FOUND),
    TOKEN_NOT_FOUND("401", "토큰이 존재하지 않습니다", HttpStatus.UNAUTHORIZED),
    SEAT_NOT_FOUND("404", "존재하는 좌석이 없습니다", HttpStatus.NOT_FOUND),
    SEAT_NOT_AVAILABLE("409", "예약 가능한 상태가 아닙니다", HttpStatus.CONFLICT),
    RESERVATION_NOT_FOUND("404", "존재하는 예약이 없습니다", HttpStatus.NOT_FOUND),
    RESERVATION_EXPIRED("410", "예약이 만료되었습니다", HttpStatus.GONE),
    NOT_ENOUGH_POINTS("400", "포인트가 부족합니다", HttpStatus.BAD_REQUEST),
    RESERVATION_ALREADY_PAID("409", "이미 결제된 예약입니다", HttpStatus.CONFLICT),
    RESERVATION_OUTBOX_NOT_FOUND("404", "예약의 아웃박스를 찾을수 없습니다", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_SUCCESS("500", "결제가 정상처리되지 않았습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_FOUND("404", "존재하는 결제가 없습니다", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED("500", "결제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SAGA_NOT_FOUND("404", "SAGA ID 가 존재하지 않습니다", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("500", "에러가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
}