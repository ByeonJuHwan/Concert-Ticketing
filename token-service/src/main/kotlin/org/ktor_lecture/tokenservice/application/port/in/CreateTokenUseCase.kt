package org.ktor_lecture.tokenservice.application.port.`in`

import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenResponse
import org.ktor_lecture.tokenservice.application.service.command.CreateTokenCommand

interface CreateTokenUseCase {

    fun createToken(command: CreateTokenCommand): TokenResponse
}