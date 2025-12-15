package org.ktor_lecture.paymentservice.adapter.out.api.grpc.point

import com.concert.point.grpc.GrpcPointUseResponse
import com.concert.point.grpc.PointServiceGrpcKt
import com.concert.point.grpc.grpcPointUseRequest
import com.concert.point.grpc.grpcPointUseResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PointGrpcClient {

    private val log = LoggerFactory.getLogger(javaClass)

    @GrpcClient("user-service")
    private lateinit var pointStub: PointServiceGrpcKt.PointServiceCoroutineStub

    suspend fun use(userId: String, amount: Long): GrpcPointUseResponse {
        log.info("gRPC 포인트 사용 호출: userId=$userId, amount=$amount")

        val request = grpcPointUseRequest {
            this.userId = userId
            this.amount = amount
        }

        return try {
            val response = pointStub.usePoint(request)

            log.info("gRPC 포인트 사용 성공: remaining=${response.remainingPoints}")

            grpcPointUseResponse {
                this.userId = response.userId
                pointHistoryId = response.pointHistoryId
                remainingPoints = response.remainingPoints
            }

        } catch (e: StatusException) {
            log.error(("gRPC 포인트 사용 호출 실패: userId=$userId, amount=$amount"), e)

            when (e.status.code) {
                Status.Code.NOT_FOUND -> {
                    throw ConcertException(ErrorCode.USER_NOT_FOUND)
                }

                Status.Code.FAILED_PRECONDITION -> {
                    throw ConcertException(ErrorCode.NOT_ENOUGH_POINTS)
                }

                else -> {
                    throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            }
        } catch (e: Exception) {
            log.error("예상치 못한 에러: userId=$userId", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}