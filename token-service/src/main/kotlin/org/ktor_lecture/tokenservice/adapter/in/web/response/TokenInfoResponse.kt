package org.ktor_lecture.tokenservice.adapter.`in`.web.response


data class TokenInfoResponse(
    val token: String,
    val status: String,
    val queueOrder: Int,
    val remainingTime: Long,
)

