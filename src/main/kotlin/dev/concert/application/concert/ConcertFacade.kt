package dev.concert.application.concert

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import dev.concert.domain.service.concert.ConcertService
import dev.concert.domain.service.reservation.ReservationService
import dev.concert.domain.service.seat.SeatService
import dev.concert.domain.service.user.UserService
import dev.concert.domain.service.util.DistributedLockManager
import org.springframework.stereotype.Component

@Component
class ConcertFacade (
    private val userService: UserService,
    private val seatService : SeatService,
    private val concertService: ConcertService,
    private val reservationService: ReservationService,
    private val distributedLockManager: DistributedLockManager,
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

    fun reserveSeat(request: ConcertReservationDto): ConcertReservationResponseDto {
        val user = userService.getUser(request.userId)

        val lockValue = distributedLockManager.lock(request.seatId)

        if (lockValue != null) {
            try{
                val seat = seatService.checkAndReserveSeatTemporarily(request.seatId)
                val reservation = reservationService.saveReservation(user, seat)
                return ConcertReservationResponseDto(
                    status = reservation.status,
                    reservationExpireTime = reservation.expiresAt
                )
            } finally {
                distributedLockManager.unlock(request.seatId, lockValue)
                println("락반환 성공!!")
            }
        } else {
            throw  ConcertException(ErrorCode.LOCK_ERROR)
        }
    }
}