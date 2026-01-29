package org.ktor_lecture.userservice.adapter.out.api.grpc

import com.concert.reservation.grpc.ConcertReservationServiceGrpcKt
import com.concert.reservation.grpc.grpcConcertRequest
import com.concert.reservation.grpc.grpcReservationRequest
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchConcertsResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchReservationResponse
import org.ktor_lecture.userservice.application.port.out.UserConcertReservationGrpcClient
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserConcertReservationGrpcAdapter : UserConcertReservationGrpcClient {

    @GrpcClient("concert-service")
    private lateinit var concertStub: ConcertReservationServiceGrpcKt.ConcertReservationServiceCoroutineStub

    override suspend fun searchConcerts(userId: Long): List<SearchConcertsResponse> {
        val request = grpcConcertRequest {
            this.userId = userId
        }

        try {
            val response = concertStub.searchConcerts(request)

            return response.concertsList.map {
                    SearchConcertsResponse(
                        concertId = it.concertId,
                        concertName = it.concertName,
                        concertStartDate = it.concertStartDate,
                        concertEndDate = it.concertEndDate
                    )
                }
        } catch (e: StatusException) {
            throw e
        } catch (e: Exception) {
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    override suspend fun searchReservations(userId: Long): List<SearchReservationResponse> {

        val request = grpcReservationRequest {
            this.userId = userId
        }

        try {
            val response = concertStub.searchReservations(request)

            return response.reservationsList.map {
                SearchReservationResponse(
                    reservationId = it.reservationId,
                    reservationStatus = it.reservationStatus,
                    seatId = it.seatId,
                    price = it.price,
                )
            }

        } catch (e: StatusException) {
            throw e
        } catch (e: Exception) {
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}