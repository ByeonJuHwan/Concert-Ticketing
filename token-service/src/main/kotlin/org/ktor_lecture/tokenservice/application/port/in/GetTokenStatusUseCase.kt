package org.ktor_lecture.tokenservice.application.port.`in`

import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenInfoResponse

interface GetTokenStatusUseCase {

    fun getToken(userId: Long): TokenInfoResponse
}