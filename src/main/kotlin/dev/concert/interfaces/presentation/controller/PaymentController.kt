package dev.concert.interfaces.presentation.controller

import dev.concert.interfaces.ApiResult
import dev.concert.application.payment.PaymentFacade
import dev.concert.interfaces.presentation.request.PaymentRequest
import dev.concert.interfaces.presentation.request.toDto
import dev.concert.interfaces.presentation.response.payment.PaymentResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "결제 API", description = "예약한 좌석을 결제하는 API") 
@RestController 
@RequestMapping("/payment") 
class PaymentController ( 
    private val paymentFacade: PaymentFacade, 
) { 
 
    @Operation( 
        summary = "예약좌석 결제 API", 
        description = "예약 좌석을 결제 합니다", 
        parameters = [ 
            Parameter( 
                name = "reservationId", 
                description = "예약 키 값", 
                required = true, 
                example = "1", 
            ) 
        ], 
    ) 
    @ApiResponses( 
        ApiResponse(responseCode = "200", description = "결제 성공"), 
        ApiResponse( 
            responseCode = "404", 
            description = "존재하는 예약이 없습니다", 
            content = [Content( 
                mediaType = "application/json", 
                examples = [ 
                    ExampleObject( 
                        value = """ 
                                { 
                                    "code": 404 
                                    "message": "존재하는 예약이 없습니다" 
                                } 
                            """ 
                    ) 
                ] 
            )] 
        ), 
        ApiResponse( 
            responseCode = "410", 
            description = "예약이 만료되었습니다", 
            content = [Content( 
                mediaType = "application/json", 
                examples = [ 
                    ExampleObject( 
                        value = """ 
                                { 
                                    "code": 410 
                                    "message": "예약이 만료되었습니다" 
                                } 
                            """ 
                    ) 
                ] 
            )] 
        ), 
        ApiResponse( 
            responseCode = "400", 
            description = "포인트가 부족합니다", 
            content = [Content( 
                mediaType = "application/json", 
                examples = [ 
                    ExampleObject( 
                        value = """ 
                                { 
                                    "code": 400 
                                    "message": "포인트가 부족합니다" 
                                } 
                            """ 
                    ) 
                ] 
            )]  
        ), 
    ) 
    @PostMapping("/pay") 
    fun pay( 
        @RequestBody request: PaymentRequest
    ) : ApiResult<PaymentResponse> {
        val payment = paymentFacade.pay(request.toDto()) 
        return ApiResult(
            data = PaymentResponse.toResponse(payment) 
        ) 
    } 
}
