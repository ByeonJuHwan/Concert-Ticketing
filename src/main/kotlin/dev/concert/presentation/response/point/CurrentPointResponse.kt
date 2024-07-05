package dev.concert.presentation.response.point

import dev.concert.application.point.dto.PointResponseDto

data class CurrentPointResponse(
    val currentPoints : Long,
) {
    companion object {
        fun from(response: PointResponseDto): CurrentPointResponse {
            return CurrentPointResponse(
                currentPoints = response.point,
            )
        }
    }
}
