package org.ktor_lecture.userservice.domain.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val code: String, val message: String, val status: HttpStatus) {
    USER_NOT_FOUND("404", "존재하는 회원이 없습니다", HttpStatus.NOT_FOUND),
    TOKEN_NOT_FOUND("401", "토큰이 존재하지 않습니다", HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR("500", "에러가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
}