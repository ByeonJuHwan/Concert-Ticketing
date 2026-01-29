package org.ktor_lecture.userservice.adapter.out.api.grpc.response

data class SearchUserPaymentResponse(
    val reservationId: Long,
    val paymentId: Long,
    val paymentStatus: String,
    val paymentType: String,
    val price: Long,
)
