package org.ktor_lecture.paymentservice.adapter.out.api.grpc.concert

import com.concert.concert.grpc.ConcertServiceGrpcKt
import com.concert.concert.grpc.GrpcConcertReservationResponse
import com.concert.concert.grpc.grpcChangeReservationPaidRequest
import com.concert.concert.grpc.grpcChangeSeatReservedRequest
import com.concert.concert.grpc.grpcGetReservationRequest
import com.concert.concert.grpc.grpcReservationExpiredAndSeatAvaliableRequest
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.ktor_lecture.paymentservice.adapter.out.api.response.ConcertReservationResponse
import org.ktor_lecture.paymentservice.application.port.out.grpc.ConcertGrpcClient
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ConcertGrpcAdapter : ConcertGrpcClient {

    private val log = LoggerFactory.getLogger(javaClass)

    @GrpcClient("concert-service")
    private lateinit var concertStub: ConcertServiceGrpcKt.ConcertServiceCoroutineStub

    /**
     * GRPC: Concert-Service 예약 조회
     */
    override suspend fun getReservation(reservationId: Long): ConcertReservationResponse {
        val request = grpcGetReservationRequest {
            this.reservationId = reservationId
        }

        try {
            val reservation: GrpcConcertReservationResponse = concertStub.getReservation(request)

            return ConcertReservationResponse(
                reservationId = reservation.reservationId,
                userId = reservation.userId,
                seatId = reservation.seatId,
                seatNo = reservation.seatNo,
                status = reservation.status,
                price = reservation.price,
                expiresAt = LocalDateTime.parse(reservation.expiresAt)
            )
        } catch (e: StatusException) {
            log.error("gRPC 호출 실패", e)

            when (e.status.code) {
                Status.Code.INTERNAL -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR, e.message)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * gRPC: Concert-Service 만료된 예약 처리 및 좌석 예약 가능 상태 변경
     */
    override suspend fun reservationExpiredAndSeatAvaliable(reservationId: Long) {
        val request = grpcReservationExpiredAndSeatAvaliableRequest {
            this.reservationId = reservationId
        }
        try {
            concertStub.reservationExpiredAndSeatAvaliable(request)
        } catch (e: StatusException) {
            log.error("gRPC 호출 실패", e)

            when (e.status.code) {
                Status.Code.INTERNAL -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR, e.message)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * gRPC: Concert-Service 예약 상태를 결제 완료로 변경
     */
    override suspend fun changeReservationPaid(reservationId: Long) {
        val request = grpcChangeReservationPaidRequest {
            this.reservationId = reservationId
        }
        try {
            val response = concertStub.changeReservationPaid(request)
            log.info(response.message)
        } catch (e: StatusException) {
            log.error("예약 상태 결제 변경 gRPC 호출 실패", e)

            when (e.status.code) {
                Status.Code.INTERNAL -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR, e.message)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * gRPC: Concert-Service 좌석 예약 확정 호출
     */
    override suspend fun changeSeatReserved(reservationId: Long) {
        log.info("grpc 좌석 예약 확정 호출: reservationId = {}", reservationId)

        val request = grpcChangeSeatReservedRequest {
            this.reservationId = reservationId
        }

        try {
            val response = concertStub.changeSeatReserved(request)
            log.info(response.message)
        } catch (e: StatusException) {
            log.error("좌석 예약확정 gRPC 호출 실패", e)

            when (e.status.code) {
                Status.Code.INTERNAL -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR, e.message)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    override suspend fun changeReservationPending(reservationId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun changeSeatTemporarilyAssigned(reservationId: Long) {
        TODO("Not yet implemented")
    }
}