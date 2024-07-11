package dev.concert.application.point.facade

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.application.point.dto.PointResponseDto
import dev.concert.application.point.service.PointHistoryService
import dev.concert.application.point.service.PointService
import dev.concert.application.user.UserService
import org.springframework.stereotype.Service

@Service
class UserPointFacade (
    private val userService: UserService,
    private val pointService: PointService,
    private val pointHistoryService: PointHistoryService,
){
    fun chargePoints(request: PointRequestDto): PointResponseDto { 
        val user = userService.getUser(request.userId) 
        val point = pointService.chargePoints(user, request.amount) 
        pointHistoryService.saveChargePointHistory(user, request.amount) 
        return point 
    }

    fun getCurrentPoint(userId: Long): PointResponseDto {
        val user = userService.getUser(userId)
        return pointService.getCurrentPoint(user)
    }
}
