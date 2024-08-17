package dev.concert.domain.service.reservation

import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.event.reservation.ReservationEvent
import dev.concert.domain.event.reservation.ReservationSuccessEvent
import dev.concert.domain.event.reservation.publisher.ReservationEventPublisher
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import dev.concert.domain.repository.ReservationOutBoxRepository
import dev.concert.domain.repository.SeatRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationServiceImpl (
    private val reservationRepository : ReservationRepository,
    private val reservationOutBoxRepository : ReservationOutBoxRepository,
    private val seatRepository: SeatRepository,
    @Qualifier("Kafka") private val reservationEventPublisher: ReservationEventPublisher,
) : ReservationService {

    private val log : Logger = LoggerFactory.getLogger(ReservationServiceImpl::class.java)

    @Transactional
    override fun manageReservationStatus() {
        val expiredReservations = reservationRepository.findExpiredReservations()

        val reservationIds = expiredReservations.map { it.id }
        val seatIds = expiredReservations.map { it.seat.id }

        if (reservationIds.isNotEmpty() && seatIds.isNotEmpty()) {
            reservationRepository.updateReservationStatusToExpired(reservationIds)
            seatRepository.updateSeatStatusToAvailable(seatIds)
        }
    }

    @Transactional
    override fun createSeatReservation(user: UserEntity, seatId: Long): ReservationEntity {
        val seat = getSeat(seatId)
        changeSeatStatusTemporary(seat)
        return saveReservation(user, seat)
    }

    @Transactional(readOnly = true)
    override fun getReservation(reservationId: Long): ReservationEntity {
        return reservationRepository.findById(reservationId) ?: throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND)
    }

    /**
     * [아웃박스 패턴] BEFORE_COMMIT
     * 아웃박스에 Event 를 저장 (Init 상태)
     */
    @Transactional
    override fun saveMsgToOutBox(event: ReservationEvent) {
        reservationOutBoxRepository.save(event.toEntity())
    }

    /**
     * [아웃박스 패턴] AFTER_COMMIT
     * AFTER_COMMIT 이후의 카프카 이벤트를 발행한다
     */
    override fun publishReservationEvent(event: ReservationEvent) {
        reservationEventPublisher.publish(event)
    }

    /**
     * 발행이 실패한 이벤트들을 다시 재시도 한다
     *
     * 1. 이벤트 상태가 INIT, SEND_FAIL 인 예약 이벤트 조회
     * 2. 이벤트 재발송
     */
    override fun retryInitOrFailEvents() {
        reservationOutBoxRepository.getInitOrFailEvents().forEach { entity ->
            reservationEventPublisher.publish(ReservationSuccessEvent(entity.reservationId))
        }
    }

    /**
     * [아웃박스 패턴] 아웃박스 데이터는 3일이지나면 삭제된다
     */
    @Transactional
    override fun deleteOutBoxEvents() {
        runCatching {
            reservationOutBoxRepository.deleteEntriesOlderThanThreeDays()
        }.onFailure { e->
            log.error("ReservationOutBoxDelete Delete Error", e)
            // 슬랙 메세지, 재시도 등등
        }
    }

    private fun saveReservation(user: UserEntity, seat: SeatEntity) : ReservationEntity {
        val expiresAt = LocalDateTime.now().plusMinutes(5)

        val reservation = ReservationEntity(
            user = user,
            seat = seat,
            expiresAt = expiresAt,
        )
        return reservationRepository.saveReservation(reservation)
    }

    private fun getSeat(seatId: Long): SeatEntity {
        return seatRepository.getSeatWithLock(seatId) ?: throw ConcertException(ErrorCode.SEAT_NOT_FOUND)
    }

    private fun changeSeatStatusTemporary(seat: SeatEntity) {
        checkSeatAvailable(seat)
        seat.changeSeatStatus(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    private fun checkSeatAvailable(seat: SeatEntity) {
        if (seat.seatStatus != SeatStatus.AVAILABLE){
            throw ConcertException(ErrorCode.SEAT_NOT_AVAILABLE)
        }
    }
}