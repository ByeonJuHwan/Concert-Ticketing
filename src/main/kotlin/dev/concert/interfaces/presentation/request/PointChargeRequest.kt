package dev.concert.interfaces.presentation.request

import dev.concert.application.point.dto.PointRequestDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "포인트 충전 요청")
data class PointChargeRequest(

    @Schema(description = "사용자 ID", example = "1")
    val userId : Long,

    @Schema(description = "충전할 포인트 금액", example = "1000")
    val amount : Long,
)

fun PointChargeRequest.toDto() = PointRequestDto (
    userId = userId,
    amount = amount,
)
