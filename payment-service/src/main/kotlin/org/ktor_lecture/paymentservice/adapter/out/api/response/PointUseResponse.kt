package org.ktor_lecture.paymentservice.adapter.out.api.response

data class PointUseResponse(
    val userId: Long,
    val pointHistoryId: Long,
    val remainingPoints: Long,
)
