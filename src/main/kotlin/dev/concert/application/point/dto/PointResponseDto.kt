package dev.concert.application.point.dto

import dev.concert.domain.entity.PointEntity

data class PointResponseDto(
    val point : Long,
){
    companion object {
        fun from(entity: PointEntity): PointResponseDto {
            return PointResponseDto(
                point = entity.point,
            )
        }
    }
}