package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.application.point.facade.PointFacade
import dev.concert.presentation.request.PointChargeRequest
import dev.concert.presentation.request.toDto
import dev.concert.presentation.response.point.CurrentPointResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/points")
class PointController (
    private val pointFacade: PointFacade,
) {

    @PutMapping("/charge")
    fun pointCharge(
        @RequestBody pointRequest: PointChargeRequest,
    ): ApiResult<CurrentPointResponse> {
        val response = pointFacade.chargePoints(pointRequest.toDto())
        return ApiResult(data = CurrentPointResponse.from(response))
    }

    @GetMapping("/current/{userId}")
    fun getCurrentPoint(
        @PathVariable userId: Long,
    ): ApiResult<CurrentPointResponse> {
        val response = pointFacade.getCurrentPoint(userId)
        return ApiResult(data = CurrentPointResponse.from(response))
    }
}