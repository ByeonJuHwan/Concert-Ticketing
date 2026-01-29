package org.ktor_lecture.userservice.adapter.out.api.grpc.response

data class SearchUserReservationListResponse(
    val reservations: List<SearchUserReservationResponse>
)

data class SearchUserReservationResponse(
    val reservationId: Long,
    val reservationStatus: String,
)
