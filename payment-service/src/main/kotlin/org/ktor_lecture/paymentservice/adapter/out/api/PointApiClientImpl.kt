package org.ktor_lecture.paymentservice.adapter.out.api

import org.ktor_lecture.paymentservice.adapter.out.api.request.point.PointCancelRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.point.PointConfirmRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.point.PointReserveRequest
import org.ktor_lecture.paymentservice.adapter.out.api.request.point.PointUseRequest
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
import org.springframework.web.client.RestClient

class PointApiClientImpl(
    private val restClient: RestClient,
): PointApiClient {


    override fun reservePoints(requestId: String, userId: String, reserveAmount: Long) {
        val request = PointReserveRequest(
            requestId = requestId,
            userId = userId,
            reserveAmount = reserveAmount,
        )

        restClient.post()
            .uri("/points/reserve")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    override fun conformPoints(requestId: String) {
        val request = PointConfirmRequest(
            requestId = requestId,
        )

        restClient.post()
            .uri("/points/confirm")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    override fun use(requestId: String, userId: String, amount: Long) {
        val request = PointUseRequest(
            requestId = requestId,
            userId = userId,
            amount = amount,
        )

        restClient.post()
            .uri("/points/use")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    override fun cancel(userId: String, amount: Long) {
        val request = PointCancelRequest(
            userId = userId,
            amount = amount,
        )

        restClient.post()
            .uri("/points/cancel")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}