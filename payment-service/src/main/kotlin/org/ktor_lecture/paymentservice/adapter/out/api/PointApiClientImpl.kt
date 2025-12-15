package org.ktor_lecture.paymentservice.adapter.out.api

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.ktor_lecture.paymentservice.adapter.out.api.request.point.PointCancelRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.point.PointUseRequest
import org.ktor_lecture.paymentservice.adapter.out.api.response.PointUseResponse
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestClient

@Primary
open class PointApiClientImpl(
    private val restClient: RestClient,
): PointApiClient {

    @CircuitBreaker(name = "userService")
    override fun use(userId: String, amount: Long): PointUseResponse {
        val request = PointUseRequest(
            userId = userId,
            amount = amount,
        )

        return restClient.post()
            .uri("/points/use")
            .body(request)
            .retrieve()
            .body(PointUseResponse::class.java)
            ?: throw IllegalStateException("Point use response is null")
    }

    override fun cancel(userId: String, pointHistoryId: Long, price: Long) {
        val request = PointCancelRequest(
            userId = userId,
            pointHistoryId = pointHistoryId,
            amount = price,
        )

        restClient.post()
            .uri("/points/cancel")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}