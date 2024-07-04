package dev.concert.presentation

import dev.concert.ApiResult
import dev.concert.infrastructure.entity.ConcertEntity
import dev.concert.infrastructure.entity.status.ReservationStatus
import dev.concert.presentation.request.PaymentRequest
import dev.concert.presentation.request.SeatRequest
import dev.concert.presentation.response.concert.ConcertAvailableDate
import dev.concert.presentation.response.concert.ConcertAvailableDateResponse
import dev.concert.presentation.response.concert.ConcertAvailableSeatsResponse
import dev.concert.presentation.response.concert.ConcertResponse
import dev.concert.presentation.response.concert.ConcertSeat
import dev.concert.presentation.response.concert.PaymentResponse
import dev.concert.presentation.response.concert.SeatResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/concerts")
class ConcertController {

    @GetMapping
    fun getAllConcerts(): ApiResult<ConcertResponse> {
        val list : List<ConcertEntity>  = listOf(
            ConcertEntity(
                concertName = "아이유 콘서트",
                singer = "아이유",
                startDate = "2024-02-01",
                endDate = "2024-02-12",
                reserveStartDate = "2024-01-01",
                reserveEndDate = "2024-01-31",
            ),
            ConcertEntity(
                concertName = "에스파 콘서트",
                singer = "에스파",
                startDate = "2024-02-01",
                endDate = "2024-02-12",
                reserveStartDate = "2024-01-01",
                reserveEndDate = "2024-01-31",
            ),
        )
        return ApiResult(ConcertResponse(list))
    }

    @GetMapping("/{concertId}/dates/available")
    fun getAvailableDates(
        @PathVariable concertId : Long,
    ): ApiResult<ConcertAvailableDateResponse> {
        val list : List<ConcertAvailableDate> = listOf(
            ConcertAvailableDate(
                concertId = 1L,
                title = "에스파 콘서트",
                concertDate = "024-07-15",
                concertTime = "13:00",
                concertVenue = "잠실 종합 운동장",
                availableSeats = 50,
            ),
            ConcertAvailableDate(
                concertId = 1L,
                title = "에스파 콘서트",
                concertDate = "024-07-16",
                concertTime = "18:00",
                concertVenue = "도쿄돔",
                availableSeats = 50,
            ),
        )

        return ApiResult(ConcertAvailableDateResponse((list)))
    }

    @GetMapping("/{concertOptionId}/seats/available")
    fun getAvailableSeats(
        @PathVariable concertOptionId : Long,
    ): ApiResult<ConcertAvailableSeatsResponse> {
        val list : List<ConcertSeat> = listOf(
            ConcertSeat(
                seatId = 1L,
                seatNumber = 1,
                price = 5000,
            ),
            ConcertSeat(
                seatId = 23L,
                seatNumber = 16,
                price = 5000,
            ),
            ConcertSeat(
                seatId = 29L,
                seatNumber = 17,
                price = 10000,
            ),
        )

        return ApiResult(ConcertAvailableSeatsResponse(1L,list))
    }

    @PostMapping("/reserve-seat")
    fun reserveSeat(
        @RequestBody request: SeatRequest,
    ): ApiResult<SeatResponse> {
        return ApiResult(
            SeatResponse(
                ReservationStatus.PENDING,
                LocalDateTime.now(),
            )
        )
    }

    @PostMapping("/payments")
    fun processPayment(
        @RequestBody request: PaymentRequest,
    ): ApiResult<PaymentResponse> {
        return ApiResult(
            PaymentResponse(
                concertDate = "2025-01-01",
                concertTime = "13:00",
                concertVenue = "서울 잠실 종합운동장",
                reservationId = 1L,
                seatNo = 1
            )
        )
    }
}