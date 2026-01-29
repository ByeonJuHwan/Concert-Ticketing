package org.ktor_lecture.userservice.application.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.ktor_lecture.userservice.adapter.`in`.web.response.ConcertResponse
import org.ktor_lecture.userservice.adapter.`in`.web.response.ReservationHistoryResponse
import org.ktor_lecture.userservice.adapter.`in`.web.response.SearchConcertReservationResponse
import org.ktor_lecture.userservice.application.port.`in`.SearchConcertReservationUseCase
import org.ktor_lecture.userservice.application.port.out.UserConcertReservationGrpcClient
import org.ktor_lecture.userservice.application.port.out.UserConcertReservationHttpClient
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class ConcertReservationService (
    private val userConcertReservationGrpcClient: UserConcertReservationGrpcClient,
    private val userConcertReservationHttpClient: UserConcertReservationHttpClient,
): SearchConcertReservationUseCase {

    override suspend fun searchGrpcConcertReservationHistory(userId: Long): SearchConcertReservationResponse {
        val (concerts, reservations) = coroutineScope {
            val concertsDeferred = async { userConcertReservationGrpcClient.searchConcerts(userId) }
            val reservationsDeferred = async { userConcertReservationGrpcClient.searchReservations(userId) }

            concertsDeferred.await() to reservationsDeferred.await()
        }

        val reservationResponses = reservations.map { reservation ->
            ReservationHistoryResponse(
                reservationId = reservation.reservationId,
                reservationStatus = reservation.reservationStatus,
                seatId = reservation.seatId,
                price = reservation.price,
            )
        }

        val concertResponses = concerts.map { concert ->
            ConcertResponse(
                concertId = concert.concertId,
                concertName = concert.concertName,
                concertStartDate = concert.concertStartDate,
                concertEndDate = concert.concertEndDate
            )
        }


        return SearchConcertReservationResponse(
            userId = userId,
            reservationHistories = reservationResponses,
            concertList = concertResponses
        )
    }

    override fun searchHttpConcertReservationHistory(userId: Long): SearchConcertReservationResponse {
        val concertsFuture = CompletableFuture.supplyAsync {
            userConcertReservationHttpClient.searchConcerts(userId)
        }

        val reservationsFuture = CompletableFuture.supplyAsync {
            userConcertReservationHttpClient.searchReservations(userId)
        }

        val concerts = concertsFuture.get()
        val reservations = reservationsFuture.get()

        val reservationResponses = reservations.map { reservation ->
            ReservationHistoryResponse(
                reservationId = reservation.reservationId,
                reservationStatus = reservation.reservationStatus,
                seatId = reservation.seatId,
                price = reservation.price,
            )
        }

        val concertResponses = concerts.map { concert ->
            ConcertResponse(
                concertId = concert.concertId,
                concertName = concert.concertName,
                concertStartDate = concert.concertStartDate,
                concertEndDate = concert.concertEndDate
            )
        }


        return SearchConcertReservationResponse(
            userId = userId,
            reservationHistories = reservationResponses,
            concertList = concertResponses
        )
    }
}