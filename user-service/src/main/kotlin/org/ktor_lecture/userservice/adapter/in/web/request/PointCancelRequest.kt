package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.PointCancelCommand

data class PointCancelRequest(
    val userId: Long,
    val pointHistoryId: Long,
    val amount: Long,
) {
    fun toCommand() = PointCancelCommand(userId, pointHistoryId, amount)
}
