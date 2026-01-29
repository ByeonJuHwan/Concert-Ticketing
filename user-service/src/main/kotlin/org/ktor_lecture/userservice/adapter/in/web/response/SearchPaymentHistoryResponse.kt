package org.ktor_lecture.userservice.adapter.`in`.web.response

data class SearchPaymentHistoryResponse(
    val userId: Long,
    val paymentHistories: List<PaymentHistoryResponse>,
)

data class PaymentHistoryResponse(
    val reservationId: Long,
    val reservationStatus: String,
    val paymentId: Long?,
    val paymentStatus: String?,
    val paymentType: String?,
    val price: Long?,
)