package org.ktor_lecture.userservice.application.service.command

data class PointCancelCommand(
    val sagaId: String,
    val userId: Long,
    val pointHistoryId: Long,
    val amount: Long,
)
