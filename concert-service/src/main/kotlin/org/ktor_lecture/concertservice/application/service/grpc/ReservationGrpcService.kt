package org.ktor_lecture.concertservice.application.service.grpc

import com.concert.reservation.grpc.*
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.concertservice.application.service.ConcertReadService
import org.ktor_lecture.concertservice.application.service.ConcertReservationService
import org.slf4j.LoggerFactory

@GrpcService
class ReservationGrpcService (
    private val concertReservationService: ConcertReservationService,
    private val concertReadService: ConcertReadService,
): ConcertReservationServiceGrpcKt.ConcertReservationServiceCoroutineImplBase() {

    override suspend fun searchReservations(request: GrpcReservationRequest): GrpcReservationListResponse {

        try {
            val reservations = concertReservationService.searchUserReservations(request.userId)

            return grpcReservationListResponse {
                this.reservations.addAll(
                    reservations.map { reservation ->
                        grpcReservationResponse {
                            reservationId = reservation.reservationId
                            reservationStatus = reservation.reservationStatus
                            seatId = reservation.seatId
                            price = reservation.price
                        }
                    }
                )
            }
        }  catch (e: Exception) {
            throw StatusException(
                Status.INTERNAL
                    .withDescription("예약 정보 조회 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }

    override suspend fun searchConcerts(request: GrpcConcertRequest): GrpcConcertListResponse {
        try {
            val concerts = concertReadService.getConcerts(null, null, null, null)

            return grpcConcertListResponse {
                this.concerts.addAll(
                    concerts.map { concert ->
                        grpcConcertResponse {
                            concertId = concert.id
                            concertName = concert.concertName
                            concertStartDate = concert.startDate
                            concertEndDate = concert.endDate
                        }
                    }
                )
            }
        }  catch (e: Exception) {
            throw StatusException(
                Status.INTERNAL
                    .withDescription("예약 정보 조회 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }
}