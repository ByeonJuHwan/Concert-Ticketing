package org.ktor_lecture.paymentservice.adapter.`in`.web.api.internal

import org.ktor_lecture.paymentservice.application.service.PaymentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
class PaymentInternalController (
    private val paymentService: PaymentService,
) {

    @GetMapping("/users/{userId}")
    fun searchUserPayments(@PathVariable userId: Long): List<SearchUserPaymentResponse> {
        val payments = paymentService.searchUserPayments(userId)
        return payments.map {
            SearchUserPaymentResponse(
                reservationId = it.reservationId,
                paymentId = it.id!!,
                paymentStatus = it.paymentStatus.name,
                paymentType = it.paymentType.name,
                price = it.price,
            )
        }
    }
}

data class SearchUserPaymentResponse(
    val reservationId: Long,
    val paymentId: Long,
    val paymentStatus: String,
    val paymentType: String,
    val price: Long,
)
