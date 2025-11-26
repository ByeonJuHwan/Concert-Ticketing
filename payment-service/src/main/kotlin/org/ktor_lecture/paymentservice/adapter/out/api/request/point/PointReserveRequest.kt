package org.ktor_lecture.paymentservice.adapter.out.api.request.point

data class PointReserveRequest(
    val requestId: String,
    val userId: String,
    val reserveAmount: Long,
)
