package dev.concert.application.point.facade

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.application.point.dto.PointResponseDto
import dev.concert.application.point.service.PointService
import org.springframework.stereotype.Service

@Service
class PointFacade (
    private val pointService: PointService,
){
    fun chargePoints(request: PointRequestDto): PointResponseDto {
        return pointService.chargePoints(request)
    }

    fun getCurrentPoint(userId: Long): PointResponseDto {
        return pointService.getCurrentPoint(userId)
    }
}