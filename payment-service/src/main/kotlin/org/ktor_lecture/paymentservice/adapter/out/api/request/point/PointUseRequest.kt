package org.ktor_lecture.paymentservice.adapter.out.api.request.point

data class PointUseRequest(
    val userId: Long,
    val amount: Long,
)
