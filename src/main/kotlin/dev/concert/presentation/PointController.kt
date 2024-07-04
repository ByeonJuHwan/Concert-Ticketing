package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.presentation.request.PointChargeRequest
import dev.concert.presentation.response.point.CurrentPointResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/points")
class PointController {

    @PutMapping("/charge")
    fun pointCharge(
        @RequestBody pointRequest: PointChargeRequest,
    ): ApiResult<CurrentPointResponse> {
        return ApiResult(
            CurrentPointResponse(5000)
        )
    }

    @GetMapping("/current/{userId}")
    fun getPoints(
        @PathVariable userId: Long,
    ): ApiResult<CurrentPointResponse> {
        return ApiResult(
            CurrentPointResponse(5000)
        )
    }
}