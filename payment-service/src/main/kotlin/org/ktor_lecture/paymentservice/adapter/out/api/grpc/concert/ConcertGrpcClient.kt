package org.ktor_lecture.paymentservice.adapter.out.api.grpc.concert

import com.concert.concert.grpc.ConcertServiceGrpcKt
import com.concert.concert.grpc.grpcChangeReservationPaidRequest
import com.concert.concert.grpc.grpcChangeSeatReservedRequest
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConcertGrpcClient {

    private val log = LoggerFactory.getLogger(javaClass)

    @GrpcClient("concert-service")
    private lateinit var concertStub: ConcertServiceGrpcKt.ConcertServiceCoroutineStub

    suspend fun changeReservationPaid (reservationId: String) {
        log.info("gRPC 예약 확정 호출: reservationId=$reservationId")

        val request = grpcChangeReservationPaidRequest {
            this.reservationId = reservationId
        }

        return try {
            concertStub.changeReservationPaid(request)

            log.info("gRPC 예약 확정 성공: reservationId=$reservationId")
        } catch (e: StatusException) {
            log.error("gRPC 호출 실패", e)

            when (e.status.code) {
                Status.Code.NOT_FOUND -> {
                    throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            }

        } catch (e: Exception) {
            log.error("예상치 못한 에러 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    suspend fun changeSeatReserved(requestId: String) {
        log.info("grpc 좌석 예약 확정 호출: requestId = {}", requestId)

        val request = grpcChangeSeatReservedRequest {
            this.requestId = requestId
        }

        try {
            concertStub.changeSeatReserved(request)
        } catch (e: StatusException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> {
                    throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND)
                }

                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}