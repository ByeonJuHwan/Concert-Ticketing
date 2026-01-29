package org.ktor_lecture.userservice.application.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.ktor_lecture.userservice.adapter.`in`.web.response.PaymentHistoryResponse
import org.ktor_lecture.userservice.adapter.`in`.web.response.SearchPaymentHistoryResponse
import org.ktor_lecture.userservice.adapter.out.api.grpc.response.SearchUserPaymentResponse
import org.ktor_lecture.userservice.application.port.`in`.SearchPaymentHistoryUseCase
import org.ktor_lecture.userservice.application.port.out.UserPaymentGrpcClient
import org.ktor_lecture.userservice.application.port.out.UserPaymentHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class UserPaymentService (
    private val userPaymentGrpcClient: UserPaymentGrpcClient,
    private val userPaymentHttpClient: UserPaymentHttpClient,
): SearchPaymentHistoryUseCase {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun searchGrpcPaymentDetailHistory(userId: Long): SearchPaymentHistoryResponse {
//        val payments = userPaymentGrpcClient.searchUserPayments(userId)
//        val reservations = userPaymentGrpcClient.searchUserReservations(userId)

        val (payments, reservations) = coroutineScope {
            val paymentsDeferred = async { userPaymentGrpcClient.searchUserPayments(userId) }
            val reservationsDeferred = async { userPaymentGrpcClient.searchUserReservations(userId) }

            paymentsDeferred.await() to reservationsDeferred.await()
        }

        logger.info("reservations size: ${reservations.size}, payments size: ${payments.size}")

        val paymentMap: Map<Long, SearchUserPaymentResponse> = payments.associateBy { it.reservationId }

        val paymentHistoryResponses = reservations.map { reservation ->
            val payment = paymentMap[reservation.reservationId]
            PaymentHistoryResponse(
                reservationId = reservation.reservationId,
                reservationStatus = reservation.reservationStatus,
                paymentId = payment?.paymentId,
                paymentStatus = payment?.paymentStatus,
                paymentType = payment?.paymentType,
                price = payment?.price,
            )
        }

        return SearchPaymentHistoryResponse(
            userId = userId,
            paymentHistories = paymentHistoryResponses,
        )
    }

    override fun searchHttpPaymentDetailHistory(userId: Long): SearchPaymentHistoryResponse {
        val paymentsFuture = CompletableFuture.supplyAsync {
            userPaymentHttpClient.searchUserPayments(userId)
        }

        val reservationsFuture = CompletableFuture.supplyAsync {
            userPaymentHttpClient.searchUserReservations(userId)
        }

        val payments = paymentsFuture.get()
        val reservations = reservationsFuture.get()

        logger.info("reservations size: ${reservations.size}, payments size: ${payments.size}")

        val paymentMap: Map<Long, SearchUserPaymentResponse> = payments.associateBy { it.reservationId }

        val paymentHistoryResponses = reservations.map { reservation ->
            val payment = paymentMap[reservation.reservationId]
            PaymentHistoryResponse(
                reservationId = reservation.reservationId,
                reservationStatus = reservation.reservationStatus,
                paymentId = payment?.paymentId,
                paymentStatus = payment?.paymentStatus,
                paymentType = payment?.paymentType,
                price = payment?.price,
            )
        }

        return SearchPaymentHistoryResponse(
            userId = userId,
            paymentHistories = paymentHistoryResponses,
        )
    }
}