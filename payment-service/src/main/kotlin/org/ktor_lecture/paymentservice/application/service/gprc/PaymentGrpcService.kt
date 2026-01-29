package org.ktor_lecture.paymentservice.application.service.gprc

import com.user.payment.grpc.GrpcUserPaymentListResponse
import com.user.payment.grpc.GrpcUserPaymentRequest
import com.user.payment.grpc.UserPaymentServiceGrpcKt
import com.user.payment.grpc.grpcUserPaymentListResponse
import com.user.payment.grpc.grpcUserPaymentResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.paymentservice.application.service.PaymentService
import org.slf4j.LoggerFactory

@GrpcService
class PaymentGrpcService (
    private val paymentService: PaymentService,
) : UserPaymentServiceGrpcKt.UserPaymentServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun searchUserPayments(request: GrpcUserPaymentRequest): GrpcUserPaymentListResponse {
        log.info("gRPC 유저 결제 내역 조회 요청: userId = ${request.userId}")

        try {
            val payments = paymentService.searchUserPayments(request.userId)
            return grpcUserPaymentListResponse {
                this.payments.addAll(
                    payments.map { payment ->
                        grpcUserPaymentResponse {
                            this.reservationId = payment.reservationId
                            this.paymentId = payment.id!!
                            this.paymentStatus = payment.paymentStatus.name
                            this.paymentType = payment.paymentType.name
                            this.price = payment.price
                        }
                    }
                )
            }
        } catch (e: Exception) {
            log.error("gRPC 유저 결제 내역 조회 중 오류 발생: ${e.message}", e)
            throw StatusException(
                Status.INTERNAL
                    .withDescription("결제 정보 조회 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }
}