package org.ktor_lecture.userservice.application.service.grpc

import com.concert.point.grpc.GrpcPointCancelRequest
import com.concert.point.grpc.GrpcPointCancelResponse
import com.concert.point.grpc.GrpcPointUseRequest
import com.concert.point.grpc.GrpcPointUseResponse
import com.concert.point.grpc.PointServiceGrpcKt
import com.concert.point.grpc.grpcPointCancelResponse
import com.concert.point.grpc.grpcPointUseResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.userservice.application.service.PointService
import org.ktor_lecture.userservice.application.service.command.PointCancelCommand
import org.ktor_lecture.userservice.application.service.command.PointUseCommand
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory

@GrpcService
class PointGrpcService(
    private val pointService: PointService
) : PointServiceGrpcKt.PointServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun usePoint(request: GrpcPointUseRequest): GrpcPointUseResponse {
        log.info("gRPC 포인트 사용 요청: userId=${request.userId}, amount=${request.amount}")

        return try {
            val command = PointUseCommand(
                userId = request.userId,
                amount = request.amount
            )

            val result = pointService.use(command)

            grpcPointUseResponse {
                userId = result.userId
                pointHistoryId = result.pointHistoryId
                remainingPoints = result.remainingPoints
            }
        } catch (e: ConcertException) {
            log.error("포인트 사용 실패: code=${e.errorCode}, message=${e.message}")

            val status = when (e.errorCode) {
                ErrorCode.USER_NOT_FOUND -> Status.NOT_FOUND
                    .withDescription("사용자를 찾을수 없습니다")
                    .withCause(e)
                ErrorCode.POINT_NOT_ENOUGH -> Status.FAILED_PRECONDITION
                    .withDescription("포인트가 부족합니다")
                    .withCause(e)
                else -> Status.INTERNAL
            }

            throw StatusException (
                status.withDescription(e.message)
            )

        } catch (e: Exception) {
            log.error("포인트 사용 중 예상치 못한 오류", e)

            throw StatusException (
                Status.INTERNAL
                    .withDescription("포인트 사용 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }

    /**
     * gRPC: 포인트 취소 처리
     */
    override suspend fun cancelPoint(request: GrpcPointCancelRequest): GrpcPointCancelResponse {
        log.info("gRPC 포인트 취소 요청: userId=${request.userId}, amount=${request.amount}")

        return try {
            val command = PointCancelCommand(
                userId = request.userId,
                pointHistoryId = request.pointHistoryId,
                amount = request.amount,
                sagaId = request.sagaId,
            )

            pointService.cancel(command)

            grpcPointCancelResponse {
                success = true
                message = "포인트 사용 취소 요청 성공"
            }
        } catch (e: ConcertException) {
            log.error("포인트 사용취소 실패 code=${e.errorCode}, message=${e.message}")

            when (e.errorCode) {
                ErrorCode.USER_NOT_FOUND -> {
                    throw StatusException (
                        Status.NOT_FOUND.withDescription(e.message).withCause(e)
                    )
                }
                ErrorCode.POINT_HISTORY_NOT_FOUND -> {
                    throw StatusException (
                        Status.NOT_FOUND.withDescription(e.message).withCause(e)
                    )
                }
                else -> throw StatusException (
                    Status.INTERNAL.withDescription(e.message).withCause(e)
                )
            }
        } catch (e: Exception) {
            log.error("포인트 사용 취소 중 예상치 못한 오류", e)

            throw StatusException (
                Status.INTERNAL
                    .withDescription("포인트 사용 취소 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }
}