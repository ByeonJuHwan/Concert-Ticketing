package dev.concert.application.point

import dev.concert.application.point.dto.PointRequestDto
import dev.concert.application.point.dto.PointResponseDto
import dev.concert.domain.service.point.PointService
import dev.concert.domain.service.user.UserService
import org.springframework.stereotype.Service

@Service
class UserPointFacade (
    private val userService: UserService,
    private val pointService: PointService,
){
    fun chargePoints(request: PointRequestDto): PointResponseDto {
        val user = userService.getUser(request.userId) 
        val point = pointService.chargePoints(user, request.amount)
        return PointResponseDto(point.point)
    }

    fun getCurrentPoint(userId: Long): PointResponseDto {
        val user = userService.getUser(userId)
        return PointResponseDto(pointService.getCurrentPoint(user).point)
    }
}
