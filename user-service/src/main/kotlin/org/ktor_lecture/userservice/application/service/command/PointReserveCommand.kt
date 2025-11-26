package org.ktor_lecture.userservice.application.service.command

data class PointReserveCommand(
    val requestId: String,
    val userId: String,
    val reserveAmount: Long,
)
