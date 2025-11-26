package org.ktor_lecture.userservice.adapter.`in`.web.response

data class PointUseResponse(
    val userId: Long,
    val pointHistoryId: Long,
    val remainingPoints: Long,
)
