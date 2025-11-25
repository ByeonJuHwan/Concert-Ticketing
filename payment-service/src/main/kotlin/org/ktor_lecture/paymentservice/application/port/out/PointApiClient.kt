package org.ktor_lecture.paymentservice.application.port.out

interface PointApiClient {
    fun reservePoints(
        requestId: String,
        userId: String,
        reserveAmount: Long,
    )

    fun conformPoints(requestId: String)

    fun use(
        requestId: String,
        userId: String,
        amount: Long,
    )

    fun cancel(userId: String, price: Long)
}