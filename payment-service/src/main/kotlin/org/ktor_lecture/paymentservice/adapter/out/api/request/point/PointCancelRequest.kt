package org.ktor_lecture.paymentservice.adapter.out.api.request.point


data class PointCancelRequest(
    val userId: Long,
    val pointHistoryId: Long,
    val amount: Long,
)
