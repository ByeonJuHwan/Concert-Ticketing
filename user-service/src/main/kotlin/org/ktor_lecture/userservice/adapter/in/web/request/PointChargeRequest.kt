package org.ktor_lecture.userservice.adapter.`in`.web.request

import org.ktor_lecture.userservice.application.service.command.ChargePointCommand


data class PointChargeRequest(
    val userId : Long,
    val amount : Long,
) {
    fun toCommand() = ChargePointCommand(
        userId = userId,
        amount = amount,
    )
}

