package dev.concert.application.point.service

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.application.point.dto.PointResponseDto

interface PointService {
    fun chargePoints(request : PointRequestDto) : PointResponseDto
    fun getCurrentPoint(userId: Long): PointResponseDto
}