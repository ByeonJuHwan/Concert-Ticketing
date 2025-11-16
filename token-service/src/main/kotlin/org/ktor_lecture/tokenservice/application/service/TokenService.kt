package org.ktor_lecture.tokenservice.application.service

import org.ktor_lecture.tokenservice.adapter.`in`.web.response.TokenResponse
import org.ktor_lecture.tokenservice.application.port.`in`.CreateTokenUseCase
import org.ktor_lecture.tokenservice.application.port.out.TokenRepository
import org.ktor_lecture.tokenservice.application.service.command.CreateTokenCommand
import org.springframework.stereotype.Service

@Service
class TokenService (
    private val tokenRepository: TokenRepository,
): CreateTokenUseCase {


    override fun createToken(command: CreateTokenCommand): TokenResponse {
        TODO("Not yet implemented")
    }
}