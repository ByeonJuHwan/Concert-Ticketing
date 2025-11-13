package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.adapter.`in`.consumer.event.UserCreatedEvent
import org.ktor_lecture.concertservice.application.port.`in`.ConcertUserCreateUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReserveSeatUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.application.service.dto.ReserveSeatInfo
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConcertWriteService (
    private val concertWriteRepository: ConcertWriteRepository,
    private val concertReadRepository: ConcertReadRepository,
    private val seatRepository: SeatRepository,
    private val reservationRepository: ReservationRepository,
): ReserveSeatUseCase, ConcertUserCreateUseCase {

    @Transactional
    override fun reserveSeat(command: ReserveSeatCommand): ReserveSeatInfo {
        // 유저 정보 조회
        val user: ConcertUserEntity = concertReadRepository.findUserById(command.userId)
            .orElseThrow { ConcertException(ErrorCode.USER_NOT_FOUND) }

        // 예약가능한 좌석인지 확인하고 좌석 임시배정후 잠그기
        val seat: SeatEntity = seatRepository.getSeatWithLock(command.seatId) ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)
        seat.temporarilyReserve()

        val expiresAt = LocalDateTime.now().plusMinutes(5)
        val reservation = ReservationEntity(
            user = user,
            seat = seat,
            expiresAt = expiresAt,
        )

        reservationRepository.save(reservation)

        // TODO 예약 성공 이벤트 발행

        return ReserveSeatInfo(
            status = reservation.status,
            reservationExpireTime = reservation.expiresAt,
        )
    }

    @Transactional
    override fun createUser(event: UserCreatedEvent) {
        val user = ConcertUserEntity(
            name = event.userName,
        )

        concertWriteRepository.createUser(user)
    }
}