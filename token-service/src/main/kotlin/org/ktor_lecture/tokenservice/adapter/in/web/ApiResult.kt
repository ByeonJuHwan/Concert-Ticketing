package org.ktor_lecture.tokenservice.adapter.`in`.web

import org.springframework.http.HttpStatus

data class ApiResult<T>(
    val data: T?,
    val status: Int? = HttpStatus.OK.value(),
    val message: String? = "API 응답 성공"
)
