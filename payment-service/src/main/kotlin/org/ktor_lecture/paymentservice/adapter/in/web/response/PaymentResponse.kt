package org.ktor_lecture.paymentservice.adapter.`in`.web.response

data class PaymentResponse(
    val reservationId : Long,
    val seatNo : Int,
    val status : String,
    val price : Long,
)
