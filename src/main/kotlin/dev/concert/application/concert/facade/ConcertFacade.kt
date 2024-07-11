package dev.concert.application.concert.facade

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.application.concert.service.ConcertService
import dev.concert.application.reservation.ReservationService
import dev.concert.application.seat.SeatService
import dev.concert.application.user.UserService
import org.springframework.stereotype.Component

@Component
class ConcertFacade (
    private val userService: UserService,
    private val seatService : SeatService,
    private val concertService: ConcertService,
    private val reservationService: ReservationService,
){
    fun getConcerts(): List<ConcertsDto> { 
        return concertService.getConcerts() 
    } 

    fun getAvailableDates(concertId: Long): List<ConcertDatesDto> {
        return concertService.getAvailableDates(concertId)
    }

    fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatsDto> {
        return concertService.getAvailableSeats(concertOptionId)
    }

    fun reserveSeat(request: ConcertReservationDto): ConcertReservationResponseDto {
        // 유저 정보 조회
        val user = userService.getUser(request.userId)

        // 좌석 정보 조회(with Lock)
        val seat = seatService.getSeat(request.seatId)

        // 좌석 정보가 Available 한지 확인
        seatService.checkSeatAvailable(seat)

        // 예약 정보 저장
        // 예약 정보를 저장하고 예약 정보
        val reservation = reservationService.saveReservation(user, seat)

        // 좌석 정보를 Temporary 로 변경
        seatService.changeSeatStatusTemporary(seat)

        return ConcertReservationResponseDto(
            status = reservation.status,
            reservationExpireTime = reservation.expiresAt
        )
    }
}
