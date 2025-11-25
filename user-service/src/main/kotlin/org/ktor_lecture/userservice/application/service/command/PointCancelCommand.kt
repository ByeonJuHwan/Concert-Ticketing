package org.ktor_lecture.userservice.application.service.command

data class PointCancelCommand(
    val userId: String,
    val amount: Long,
)
