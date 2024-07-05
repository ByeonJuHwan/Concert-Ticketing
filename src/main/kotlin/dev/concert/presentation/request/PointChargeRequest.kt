package dev.concert.presentation.request

import dev.concert.application.point.dto.PointRequestDto

data class PointChargeRequest(
    val userId : Long,
    val amount : Long,
)

fun PointChargeRequest.toDto() = PointRequestDto (
    userId = userId,
    amount = amount,
)
