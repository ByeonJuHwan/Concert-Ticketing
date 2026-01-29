package org.ktor_lecture.userservice.application.port.out

import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserPaymentResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserReservationResponse

interface UserPaymentHttpClient {
    fun searchUserPayments(userId: Long): List<SearchUserPaymentResponse>
    fun searchUserReservations(userId: Long): List<SearchUserReservationResponse>
}