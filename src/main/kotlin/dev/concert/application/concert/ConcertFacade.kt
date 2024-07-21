package dev.concert.application.concert

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.domain.service.concert.ConcertService
import dev.concert.domain.service.reservation.ReservationService
import dev.concert.domain.service.seat.SeatService
import dev.concert.domain.service.user.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ConcertFacade (
    private val userService: UserService,
    private val seatService : SeatService,
    private val concertService: ConcertService,
    private val reservationService: ReservationService,
){
    fun getConcerts(): List<ConcertsDto> {
        return concertService.getConcerts().map { ConcertsDto(
            id = it.id,
            concertName = it.concertName,
            singer = it.singer,
            startDate = it.startDate,
            endDate = it.endDate,
            reserveStartDate = it.reserveStartDate,
            reserveEndDate = it.reserveEndDate,
        ) }
    }

    fun getAvailableDates(concertId: Long): List<ConcertDatesDto> {
        return concertService.getAvailableDates(concertId).map {
            ConcertDatesDto(
                concertId = it.concert.id,
            concertName = it.concert.concertName,
            availableSeats = it.availableSeats,
            concertTime = it.concertTime,
            concertVenue = it.concertVenue,
            concertDate = it.concertDate,
            )
        }
    }

    fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatsDto> {
        return concertService.getAvailableSeats(concertOptionId).map {
            ConcertSeatsDto(
                seatId = it.id,
                seatNo = it.seatNo,
                price = it.price,
                status = it.seatStatus,
            )
        }
    }

    @Transactional
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