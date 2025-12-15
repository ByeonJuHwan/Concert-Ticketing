package org.ktor_lecture.userservice.application.service.grpc

import com.concert.point.grpc.GrpcPointUseRequest
import com.concert.point.grpc.GrpcPointUseResponse
import com.concert.point.grpc.PointServiceGrpcKt
import com.concert.point.grpc.grpcPointUseResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.userservice.application.service.PointService
import org.ktor_lecture.userservice.application.service.command.PointUseCommand
import org.ktor_lecture.userservice.domain.exception.ConcertException
import org.ktor_lecture.userservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory

@GrpcService
class PointGrpcService(
    private val pointService: PointService  // 기존 서비스 재사용!
) : PointServiceGrpcKt.PointServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun usePoint(request: GrpcPointUseRequest): GrpcPointUseResponse {
        log.info("gRPC 포인트 사용 요청: userId=${request.userId}, amount=${request.amount}")

        return try {
            // 1. Command 객체 생성
            val command = PointUseCommand(
                userId = request.userId,
                amount = request.amount
            )

            // 2. 기존 비즈니스 로직 호출 (트랜잭션 포함)
            val result = pointService.use(command)

            // 3. 도메인 Response → gRPC Response 변환
            grpcPointUseResponse {
                userId = result.userId
                pointHistoryId = result.pointHistoryId
                remainingPoints = result.remainingPoints
            }
        } catch (e: ConcertException) {
            log.error("포인트 사용 실패: code=${e.errorCode}, message=${e.message}")

            val status = when (e.errorCode) {
                ErrorCode.USER_NOT_FOUND -> Status.NOT_FOUND
                ErrorCode.POINT_NOT_ENOUGH -> Status.FAILED_PRECONDITION
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
}