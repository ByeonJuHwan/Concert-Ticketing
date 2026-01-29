package org.ktor_lecture.userservice.adapter.out.api.grpc

import com.concert.reservation.grpc.ConcertReservationServiceGrpcKt
import com.concert.reservation.grpc.grpcUserReservationRequest
import com.user.payment.grpc.UserPaymentServiceGrpcKt
import com.user.payment.grpc.grpcUserPaymentRequest
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserPaymentResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserReservationResponse
import org.ktor_lecture.userservice.application.port.out.UserPaymentGrpcClient
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserPaymentGrpcAdapter : UserPaymentGrpcClient {

    private val log = LoggerFactory.getLogger(javaClass)

    @GrpcClient("payment-service")
    private lateinit var paymentStub: UserPaymentServiceGrpcKt.UserPaymentServiceCoroutineStub

    @GrpcClient("concert-service")
    private lateinit var concertStub: ConcertReservationServiceGrpcKt.ConcertReservationServiceCoroutineStub

    override suspend fun searchUserPayments(userId: Long): List<SearchUserPaymentResponse> {
        log.info("gRPC 유저 결제 내역 조회 요청: userId=${userId}")

        val request = grpcUserPaymentRequest {
            this.userId = userId
        }

        try {
            val response = paymentStub.searchUserPayments(request)

            log.info("gRPC 유저 결제 내역 조회 성공: userId=${request.userId}, paymentCount=${response.paymentsList.size}")

            return response.paymentsList.map {
                    SearchUserPaymentResponse(
                        paymentId = it.paymentId,
                        paymentStatus = it.paymentStatus,
                        paymentType = it.paymentType,
                        price = it.price,
                        reservationId = it.reservationId
                    )
                }
        } catch (e: StatusException) {
            log.error("gRPC 유저 결제 내역 조회 실패: userId=${request.userId}", e)
            throw e
        } catch (e: Exception) {
            log.error("유저 결제 내역 조회 실패", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    override suspend fun searchUserReservations(userId: Long): List<SearchUserReservationResponse> {
        log.info("gRPC 유저 예약 내역 조회 요청: userId=${userId}")

        val request = grpcUserReservationRequest {
            this.userId = userId
        }

        try {
            val response = concertStub.searchUserReservations(request)

            log.info("gRPC 유저 예약 내역 조회 성공: userId=${request.userId}")
            log.info("response.reservationsList size: ${response.reservationsList.size}")

            return response.reservationsList.map {
                SearchUserReservationResponse(
                    reservationId = it.reservationId,
                    reservationStatus = it.reservationStatus,
                )
            }

        } catch (e: StatusException) {
            log.error("gRPC 유저 결제 내역 조회 실패: userId=${request.userId}", e)
            throw e
        } catch (e: Exception) {
            log.error("유저 결제 내역 조회 실패", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}