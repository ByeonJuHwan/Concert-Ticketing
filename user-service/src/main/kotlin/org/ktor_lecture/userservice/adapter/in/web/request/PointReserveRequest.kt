package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.PointReserveCommand

data class PointReserveRequest(
    val requestId: String,
    val userId: String,
    val reserveAmount: Long,
) {
    fun toCommand(): PointReserveCommand {
        return PointReserveCommand(requestId, userId, reserveAmount)
    }
}
