package org.ktor_lecture.paymentservice.application.port.out.grpc

import org.ktor_lecture.paymentservice.adapter.out.api.response.PointUseResponse

interface PointGrpcClient {

    suspend fun use(userId: Long, amount: Long): PointUseResponse
    suspend fun cancel(userId: Long, pointHistoryId: Long, price: Long, sagaId: String)
}