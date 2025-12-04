package org.ktor_lecture.concertservice.application.service

import org.ktor_lecture.concertservice.adapter.out.search.document.ConcertDocument
import org.ktor_lecture.concertservice.domain.event.UserCreatedEvent
import org.ktor_lecture.concertservice.application.port.`in`.ConcertUserCreateUseCase
import org.ktor_lecture.concertservice.application.port.`in`.CreateConcertUseCase
import org.ktor_lecture.concertservice.application.port.`in`.ReserveSeatUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertReadRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.port.out.EventPublisher
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.command.CreateConcertCommand
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.application.service.dto.ReserveSeatInfo
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent
import org.ktor_lecture.concertservice.domain.event.ReservationCreatedEvent
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConcertWriteService (
    private val concertWriteRepository: ConcertWriteRepository,
    private val concertReadRepository: ConcertReadRepository,
    private val seatRepository: SeatRepository,
    private val reservationRepository: ReservationRepository,
    @Qualifier("application") private val eventPublisher: EventPublisher,
): ReserveSeatUseCase, ConcertUserCreateUseCase, CreateConcertUseCase {

    /**
     * 좌석 임시 예약
     *
     * 1. 유저 정보 조회
     * 2. 예약가능한 죄석인지 확인후 좌석 임시예약 -> 비관적락 사용
     * 3. 임시 예약 저장
     * 4. 예약 생성 이벤트 발행
     */
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

        val savedReservation = reservationRepository.save(reservation)

        eventPublisher.publish(ReservationCreatedEvent(savedReservation.id!!))

        return ReserveSeatInfo(
            status = reservation.status,
            reservationExpireTime = reservation.expiresAt,
        )
    }

    /**
     * USER-SERVICE 로 부터 넘어온 유저 데이터 처리
     * Kafka로 최종적 일관성으로 데이터 저장
     */
    @Transactional
    override fun createUser(event: UserCreatedEvent) {
        val user = ConcertUserEntity(
            name = event.userName,
        )

        concertWriteRepository.createUser(user)
    }

    /**
     * 콘서트를 생성한다
     *
     * 1. DB 저장
     * 2. ElasticSearch Document 저장 -> Kafka로 이관
     */
    @Transactional
    override fun createConcert(command: CreateConcertCommand) {
        val concert = ConcertEntity(
            concertName = command.concertName,
            singer = command.singer,
            startDate = command.startDate,
            endDate = command.endDate,
            reserveStartDate = command.reserveStartDate,
            reserveEndDate = command.reserveEndDate,
        )

        val savedConcert = concertWriteRepository.saveConcert(concert)

        val concertCreatedEvent = ConcertCreatedEvent(
            id = savedConcert.id!!.toString(),
            concertName = savedConcert.concertName,
            singer = savedConcert.singer,
            startDate = savedConcert.startDate.toString(),
            endDate = savedConcert.endDate.toString(),
            reserveStartDate = savedConcert.reserveStartDate.toString(),
            reserveEndDate = savedConcert.reserveEndDate.toString(),
        )

        eventPublisher.publish(concertCreatedEvent)
    }
}