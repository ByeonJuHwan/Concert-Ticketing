package org.ktor_lecture.concertservice.adapter.`in`.web

data class ApiResult<T>(
    val data: T?,
)
