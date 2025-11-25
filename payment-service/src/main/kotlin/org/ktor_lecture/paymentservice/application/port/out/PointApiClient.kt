package org.ktor_lecture.paymentservice.application.port.out

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
    )

    fun cancel(userId: String, price: Long)
}