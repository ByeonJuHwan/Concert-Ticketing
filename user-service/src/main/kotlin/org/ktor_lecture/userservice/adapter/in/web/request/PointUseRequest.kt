package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.PointUseCommand

data class PointUseRequest(
    val requestId: String,
    val userId: String,
    val amount: Long,
) {
    fun toCommand() = PointUseCommand(requestId, userId, amount)
}
