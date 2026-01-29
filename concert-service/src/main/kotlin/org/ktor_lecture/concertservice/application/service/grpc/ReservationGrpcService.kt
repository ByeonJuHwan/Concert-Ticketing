package org.ktor_lecture.concertservice.application.service.grpc

import com.concert.reservation.grpc.ConcertReservationServiceGrpcKt
import com.concert.reservation.grpc.GrpcUserReservationListResponse
import com.concert.reservation.grpc.GrpcUserReservationRequest
import com.concert.reservation.grpc.grpcUserReservationListResponse
import com.concert.reservation.grpc.grpcUserReservationResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.concertservice.application.service.ConcertReservationService
import org.slf4j.LoggerFactory

@GrpcService
class ReservationGrpcService (
    private val concertReservationService: ConcertReservationService,
): ConcertReservationServiceGrpcKt.ConcertReservationServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun searchUserReservations(request: GrpcUserReservationRequest): GrpcUserReservationListResponse {
        log.info("gRPC 유저 예약 정보 조회 : userId: ${request.userId}")

        try {
            val reservations = concertReservationService.searchUserReservations(request.userId)
            log.info("reservations : ${reservations.size}")

            return grpcUserReservationListResponse {
                this.reservations.addAll(
                    reservations.map { reservation ->
                        grpcUserReservationResponse {
                            reservationId = reservation.reservationId
                            reservationStatus = reservation.reservationStatus
                        }
                    }
                )
            }
        }  catch (e: Exception) {
            log.error("gRPC 유저 예약 정보 조회 중 알 수 없는 오류 발생 : userId: ${request.userId}", e)
            throw StatusException(
                Status.INTERNAL
                    .withDescription("예약 정보 조회 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }
}