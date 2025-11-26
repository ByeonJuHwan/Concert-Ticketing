package org.ktor_lecture.paymentservice.application.port.out

import org.ktor_lecture.paymentservice.adapter.out.api.response.PointUseResponse

interface PointApiClient {
    fun reservePoints(
        requestId: String,
        userId: String,
        reserveAmount: Long,
    )

    fun conformPoints(requestId: String)

    fun use(
        userId: String,
        amount: Long,
    ): PointUseResponse

    fun cancel(userId: String, pointHistoryId: Long, price: Long)
}