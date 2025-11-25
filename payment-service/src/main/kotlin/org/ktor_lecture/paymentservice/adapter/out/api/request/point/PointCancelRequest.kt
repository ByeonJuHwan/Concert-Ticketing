package org.ktor_lecture.paymentservice.adapter.out.api.request.point


data class PointCancelRequest(
    val userId: String,
    val amount: Long,
)
