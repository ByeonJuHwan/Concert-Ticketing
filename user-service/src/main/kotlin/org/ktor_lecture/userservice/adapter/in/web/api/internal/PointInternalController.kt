package org.ktor_lecture.userservice.adapter.`in`.web.api.internal

import org.ktor_lecture.userservice.adapter.`in`.web.request.PointCancelRequest
import org.ktor_lecture.userservice.adapter.`in`.web.request.PointUseRequest
import org.ktor_lecture.userservice.adapter.`in`.web.response.CurrentPointResponse
import org.ktor_lecture.userservice.application.port.`in`.point.PointCancelUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.PointUseUseCase
import org.ktor_lecture.userservice.application.port.`in`.point.SearchCurrentPointsUseCase
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/points")
class PointInternalController (
    private val searchCurrentPointsUseCase: SearchCurrentPointsUseCase,
    private val pointUseUseCase: PointUseUseCase,
    private val pointCancelUseCase: PointCancelUseCase,
) {

    @GetMapping("/current/{userId}")
    fun getCurrentPoint(
        @PathVariable userId: Long,
    ): CurrentPointResponse {
        return searchCurrentPointsUseCase.getCurrentPoint(userId)
    }


    @PostMapping("/use")
    fun use(
        @RequestBody request: PointUseRequest,
    ) {
        pointUseUseCase.use(request.toCommand())
    }

    @PostMapping("/cancel")
    fun cancel(
        @RequestBody request: PointCancelRequest,
    ) {
        pointCancelUseCase.cancel(request.toCommand())
    }
}