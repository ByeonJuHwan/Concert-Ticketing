package org.ktor_lecture.paymentservice.adapter.`in`.web.api

import org.ktor_lecture.paymentservice.adapter.`in`.web.ApiResult
import org.ktor_lecture.paymentservice.adapter.`in`.web.request.PaymentRequest
import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
class PaymentController (
    private val paymentUseCase: PaymentUseCase,
) {

    @PostMapping("/pay")
    fun pay(
        @RequestBody request: PaymentRequest
    ) : ApiResult<PaymentResponse> {
        val payment = paymentUseCase.pay(request.toCommand())
        return ApiResult(
            data = payment,
            message = "결제 완료"
        )
    }
}