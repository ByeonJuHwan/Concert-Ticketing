package org.ktor_lecture.paymentservice.adapter.out.api.grpc.point

import kotlinx.coroutines.runBlocking
import org.ktor_lecture.paymentservice.adapter.out.api.response.PointUseResponse
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * suspend 함수를 포함하고 있는 gRPC Client 를 호출하기 위한 어댑터
 * 코루틴을 직접 사용하지 않는 서비스 계층에서 gRPC Client 를 호출하기 위해 어댑터 패턴 적용
 */
@Component
class PointGrpcAdapter (
    private val pointGrpcClient: PointGrpcClient,
): PointApiClient {

    // TODO : 추후 코루틴에 대해서 공부하면 runBlocking 제거하기
    override fun use(userId: String, amount: Long,): PointUseResponse {
        return runBlocking {
            val response = pointGrpcClient.use(userId, amount)

            PointUseResponse(
                userId = response.userId,
                pointHistoryId = response.pointHistoryId,
                remainingPoints = response.remainingPoints
            )
        }
    }

    override fun cancel(userId: String, pointHistoryId: Long, price: Long) {
        TODO("Not yet implemented")
    }
}