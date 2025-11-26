package org.ktor_lecture.userservice.adapter.`in`.web.api

import org.ktor_lecture.userservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.userservice.adapter.`in`.web.request.PointChargeRequest
import org.ktor_lecture.userservice.adapter.`in`.web.response.CurrentPointResponse
import org.ktor_lecture.userservice.application.port.`in`.point.ChargePointUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.SearchCurrentPointsUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/points")
class PointController (
    private val chargePointUseCase: ChargePointUseCase,
    private val searchCurrentPointsUseCase: SearchCurrentPointsUseCase,
) {


    @PutMapping("/charge") 
    fun pointCharge(
        @RequestBody pointRequest: PointChargeRequest,
    ): ApiResult<CurrentPointResponse> {
        val response = chargePointUseCase.chargePoints(pointRequest.toCommand())
        return ApiResult(
            data = response,
            message = "포인트 충전 성공"
        )
    } 


    @GetMapping("/current/{userId}") 
    fun getCurrentPoint( 
        @PathVariable userId: Long, 
    ): ApiResult<CurrentPointResponse> {
        val response = searchCurrentPointsUseCase.getCurrentPoint(userId)
        return ApiResult(data = response)
    } 
} 
