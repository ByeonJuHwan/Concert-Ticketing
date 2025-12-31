package org.ktor_lecture.paymentservice.application.port.out.http

import org.ktor_lecture.paymentservice.adapter.out.api.response.PointUseResponse

interface PointApiClient {

    fun use(
        userId: String,
        amount: Long,
    ): PointUseResponse

    fun cancel(userId: String, pointHistoryId: Long, price: Long)
}