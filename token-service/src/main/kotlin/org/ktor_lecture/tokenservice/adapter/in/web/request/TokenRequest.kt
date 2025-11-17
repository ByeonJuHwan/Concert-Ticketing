package org.ktor_lecture.tokenservice.adapter.`in`.web.request

import org.ktor_lecture.tokenservice.application.service.command.CreateTokenCommand

data class TokenRequest(
    val userId : Long,
) {
    fun toCommand() = CreateTokenCommand(userId)
}
