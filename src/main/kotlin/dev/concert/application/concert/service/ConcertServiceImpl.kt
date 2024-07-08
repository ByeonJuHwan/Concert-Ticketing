package dev.concert.application.concert.service

import dev.concert.application.concert.dto.ConcertDatesDto
import dev.concert.application.concert.dto.ConcertReservationDto
import dev.concert.application.concert.dto.ConcertReservationResponseDto
import dev.concert.application.concert.dto.ConcertSeatsDto
import dev.concert.application.concert.dto.ConcertsDto
import dev.concert.domain.ConcertRepository
import dev.concert.domain.ReservationRepository
import dev.concert.domain.UserRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.exception.NotFoundSeatException
import dev.concert.exception.SeatIsNotAvailableException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConcertServiceImpl (
    private val concertRepository: ConcertRepository,
    private val reservationRepository : ReservationRepository,
    private val userRepository: UserRepository,
) : ConcertService {
    override fun getConcerts(): List<ConcertsDto> {
        return concertRepository.getConcerts().map { ConcertsDto(
            id = it.id,
            concertName = it.concertName,
            singer = it.singer,
            startDate = it.startDate,
            endDate = it.endDate,
            reserveStartDate = it.reserveStartDate,
            reserveEndDate = it.reserveEndDate,
        ) }
    }

    override fun getAvailableDates(concertId: Long): List<ConcertDatesDto> {
        return concertRepository.getAvailableDates(concertId).map { ConcertDatesDto(
            concertId = it.concert.id,
            concertName = it.concert.concertName,
            availableSeats = it.availableSeats,
            concertTime = it.concertTime,
            concertVenue = it.concertVenue,
            concertDate = it.concertDate,
        )}
    }

    override fun getAvailableSeats(concertOptionId: Long): List<ConcertSeatsDto> {
        return concertRepository.getAvailableSeats(concertOptionId).map { ConcertSeatsDto(
            seatId = it.id,
            seatNo = it.seatNo,
            price = it.price,
            status = it.seatStatus,
        ) }
    }

    @Transactional
    override fun reserveSeat(request: ConcertReservationDto): ConcertReservationResponseDto {
        val user = getUser(request.userId)

        // 자리를 가져온다
        // 동시에 2명이 접근해서 같은 자리를 예약할 수 없도록 처리(동시성 이슈처리) 해야함
        val seat = getSeat(request.seatId)

        // 예약좌석이 Available 상태인지 확인한다
        checkSeatStatusAvailable(seat)

        // 5분간 예약의 유예시간을 설정한 예약 객체를 생성한다
        val expiresAt = LocalDateTime.now().plusMinutes(5)
        val reservation = createReservationEntity(user, seat, expiresAt)

        // 예약을 저장한다
        reservationRepository.reserveSeat(reservation)

        // 예약된 자리의 상태를 TEMPORARILY_ASSIGNED 로 변경한다
        changeSeatStatusTemporarilyAssigned(seat)

        return ConcertReservationResponseDto(
            status = reservation.status,
            reservationExpireTime = reservation.expiresAt,
        )
    }

    private fun changeSeatStatusTemporarilyAssigned(seat: SeatEntity) {
        seat.changeStatus(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    private fun createReservationEntity(
        user: UserEntity,
        seat: SeatEntity,
        expiresAt: LocalDateTime
    ) = ReservationEntity(
        user = user,
        seatNo = seat.seatNo,
        price = seat.price,
        concertName = seat.concertOption.concert.concertName,
        concertDate = seat.concertOption.concertDate,
        concertTime = seat.concertOption.concertTime,
        concertVenue = seat.concertOption.concertVenue,
        expiresAt = expiresAt,
    )

    private fun checkSeatStatusAvailable(seat: SeatEntity) {
        if (seat.seatStatus != SeatStatus.AVAILABLE) {
            throw SeatIsNotAvailableException("예약 가능한 상태가 아닙니다")
        }
    }

    private fun getSeat(seatId: Long) =
        concertRepository.getSeatWithLock(seatId) ?: throw NotFoundSeatException("좌석이 존재하지 않습니다")

    private fun getUser(userId: Long): UserEntity {
        return userRepository.findById(userId) ?: throw IllegalArgumentException("사용자가 존재하지 않습니다")
    }
}