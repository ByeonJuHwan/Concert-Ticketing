package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.PointConfirmCommand

data class PointConfirmRequest (
    val requestId: String,
) {
    fun toCommand() = PointConfirmCommand(requestId)
}