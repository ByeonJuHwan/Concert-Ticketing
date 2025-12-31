package org.ktor_lecture.paymentservice.adapter.out.api.grpc.point

import com.concert.point.grpc.GrpcPointUseResponse
import com.concert.point.grpc.PointServiceGrpcKt
import com.concert.point.grpc.grpcPointCancelRequest
import com.concert.point.grpc.grpcPointUseRequest
import com.concert.point.grpc.grpcPointUseResponse
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.client.inject.GrpcClient
import org.ktor_lecture.paymentservice.adapter.out.api.response.PointUseResponse
import org.ktor_lecture.paymentservice.application.port.out.grpc.PointGrpcClient
import org.ktor_lecture.paymentservice.application.port.out.http.PointApiClient
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * suspend 함수를 포함하고 있는 gRPC Client 를 호출하기 위한 어댑터
 * 코루틴을 직접 사용하지 않는 서비스 계층에서 gRPC Client 를 호출하기 위해 어댑터 패턴 적용
 */
@Component
class PointGrpcAdapter : PointGrpcClient {

    private val log = LoggerFactory.getLogger(javaClass)

    @GrpcClient("user-service")
    private lateinit var pointStub: PointServiceGrpcKt.PointServiceCoroutineStub

    override suspend fun use(userId: Long, amount: Long): PointUseResponse {
        log.info("gRPC 포인트 사용 호출: userId=$userId, amount=$amount")

        val request = grpcPointUseRequest {
            this.userId = userId
            this.amount = amount
        }

        try {
            val response = pointStub.usePoint(request)

            log.info("gRPC 포인트 사용 성공: remaining=${response.remainingPoints}")

            return PointUseResponse(
                userId = response.userId,
                pointHistoryId = response.pointHistoryId,
                remainingPoints = response.remainingPoints,
            )

        } catch (e: StatusException) {
            log.error(("gRPC 포인트 사용 호출 실패: userId=$userId, amount=$amount"), e)

            when (e.status.code) {
                Status.Code.INTERNAL -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR, e.message)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러: userId=$userId", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    override suspend fun cancel(userId: Long, pointHistoryId: Long, price: Long) {
        log.info("gRPC 포인트 취소 호출: userId=$userId")

        val request = grpcPointCancelRequest {
            this.userId = userId
            this.pointHistoryId = pointHistoryId
            this.amount = price
        }

        try {
            val response = pointStub.cancelPoint(request)
            log.info("gRPC 포인트 취소 성공: userId=$userId, message = ${response.message}")
        } catch (e: StatusException) {
            log.error("gRPC 포인트 사용 호출 실패: userId=$userId, price = $price", e)

            when (e.status.code) {
                Status.Code.INTERNAL -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR, e.message)
                }
            }

        } catch (e: Exception) {
            log.error("예상치 못한 에러: userId=$userId", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}