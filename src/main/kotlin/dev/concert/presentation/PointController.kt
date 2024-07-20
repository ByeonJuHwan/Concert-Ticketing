package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.application.point.facade.UserPointFacade
import dev.concert.presentation.request.PointChargeRequest
import dev.concert.presentation.request.toDto
import dev.concert.presentation.response.point.CurrentPointResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "포인트 API", description = "포인트를 충전하거나 조회하는 API")
@RestController
@RequestMapping("/points")
class PointController (
    private val userPointFacade: UserPointFacade,
) {

    @Operation( 
        summary = "포인트 충전 API", 
        description = "포인트를 충전합니다.", 
    ) 
    @ApiResponses( 
        ApiResponse(responseCode = "200", description = "포인트 충전 성공"), 
    ) 
    @PutMapping("/charge") 
    fun pointCharge( 
        @RequestBody pointRequest: PointChargeRequest, 
    ): ApiResult<CurrentPointResponse> { 
        val response = userPointFacade.chargePoints(pointRequest.toDto()) 
        return ApiResult(data = CurrentPointResponse.from(response)) 
    } 

    @Operation( 
        summary = "포인트 조회 API", 
        description = "포인트를 조회합니다.", 
        parameters = [ 
            Parameter( 
                name = "userId", 
                description = "사용자 키 값", 
                required = true, 
                example = "1", 
            ) 
        ], 
    ) 
    @ApiResponses( 
        ApiResponse(responseCode = "200", description = "포인트 조회 성공"), 
        ApiResponse( 
            responseCode = "404", 
            description = "사용자를 찾을 수 없음", 
            content = [Content( 
                mediaType = "application/json", 
                examples = [ 
                    ExampleObject(  
                        value = """ 
                                { 
                                    "code": 404 
                                    "message": "사용자를 찾을 수 없습니다." 
                                } 
                            """ 
                    ) 
                ] 
            )] 
        ), 
    ) 
    @GetMapping("/current/{userId}") 
    fun getCurrentPoint( 
        @PathVariable userId: Long, 
    ): ApiResult<CurrentPointResponse> { 
        val response = userPointFacade.getCurrentPoint(userId) 
        return ApiResult(data = CurrentPointResponse.from(response)) 
    } 
} 
