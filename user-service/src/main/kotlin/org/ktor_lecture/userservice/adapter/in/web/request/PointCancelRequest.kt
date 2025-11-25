package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.PointCancelCommand

data class PointCancelRequest(
    val userId: String,
    val amount: Long,
) {
    fun toCommand() = PointCancelCommand(userId, amount)
}
