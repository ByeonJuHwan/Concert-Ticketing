package org.ktor_lecture.userservice.application.service.command

data class PointUseCommand(
    val requestId: String,
    val userId: String,
    val amount: Long,
)
