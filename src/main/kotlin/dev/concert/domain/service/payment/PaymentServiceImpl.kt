package dev.concert.domain.service.payment

import dev.concert.domain.repository.PaymentRepository
import dev.concert.domain.entity.PaymentEntity
import dev.concert.domain.entity.PointEntity
import dev.concert.domain.entity.PointHistoryEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.UserEntity
import dev.concert.domain.entity.status.PaymentStatus
import dev.concert.domain.entity.status.PaymentType
import dev.concert.domain.entity.status.PointTransactionType
import dev.concert.domain.entity.status.ReservationStatus
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.exception.ConcertException
import dev.concert.domain.exception.ErrorCode
import dev.concert.domain.repository.PointHistoryRepository
import dev.concert.domain.repository.PointRepository
import dev.concert.domain.repository.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service 
class PaymentServiceImpl(
    private val paymentRepository : PaymentRepository,
    private val reservationRepository : ReservationRepository,
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    ) : PaymentService {

    /**
     * 결제 처리
     *
     * 1. 예약 정보 조회
     * 2. 이미 결재한 예약인지 확인한다 (이미 결재 혹은 만료된 예약은 예외를 발생시킨다)
     * 3. 임시 좌석 저장인 5분 안에 결재 요청을 했는지 확인한다 expiresAt 으로 확인(만료되었다면 상태를 Expired로 변경하고 예외를 터트린다)
     * 4. 포인트 상태를 확인한다 (포인트가 price 보다 적으면 예외를 터트린다)
     * 5. 포인트 차감 -> 결제를 진행한다 (카드 무통장등등 많지만 요구사항은 포인트 이므로 포인트로 진행)
     * 6. 포인트 차감 히스토리 저장
     * 7. 예약 상태 변경 -> 결제 완료로 변경
     * 8. 좌석 상태 변경 -> 예약 완료로 변경
     * 9. 예약 정보 저장
     */
    @Transactional
    override fun processReservationPayment(reservationId: Long) : PaymentEntity {
        val reservation = reservationRepository.findById(reservationId)?: throw ConcertException(ErrorCode.RESERVATION_NOT_FOUND)
        checkReservationStatus(reservation)
        isExpired(reservation)

        val user = reservation.user
        val price = reservation.seat.price

        usePoints(user, price)

        reservation.changeStatus(ReservationStatus.PAID)
        reservation.seat.changeSeatStatus(SeatStatus.RESERVED)

        return createPayments(reservation)
    }

    private fun usePoints(user: UserEntity, price: Long) {
        checkPoint(user, price).deduct(price)
        savePointUseHistory(user, price)
    }

    private fun savePointUseHistory(user: UserEntity, price: Long) {
        val history = createHistoryEntity(user, price, PointTransactionType.USE)
        pointHistoryRepository.saveHistory(history)
    }

    private fun checkPoint(user: UserEntity, price: Long) : PointEntity {
        val currentPoint = pointRepository.findByUser(user) ?: PointEntity(user, 0)
        if(currentPoint.point < price){
            throw ConcertException(ErrorCode.NOT_ENOUGH_POINTS)
        }
        return currentPoint
    }

    private fun isExpired(reservation: ReservationEntity) {
        if(LocalDateTime.now().isAfter(reservation.expiresAt)){
            reservation.changeStatus(ReservationStatus.EXPIRED)
            reservation.seat.changeSeatStatus(SeatStatus.AVAILABLE)
            throw ConcertException(ErrorCode.RESERVATION_EXPIRED)
        }
    }

    private fun checkReservationStatus(reservation: ReservationEntity) {
        when (reservation.status) {
            ReservationStatus.PAID -> throw ConcertException(ErrorCode.RESERVATION_ALREADY_PAID)
            ReservationStatus.EXPIRED -> throw ConcertException(ErrorCode.RESERVATION_EXPIRED)
            else -> return
        }
    }

    private fun createHistoryEntity(
        user: UserEntity,
        amount: Long,
        type: PointTransactionType
    ): PointHistoryEntity {
        val history = PointHistoryEntity(
            user = user,
            amount = amount,
            type = type,
        )
        return history
    }

    private fun createPayments(reservation: ReservationEntity) : PaymentEntity {
        val payment = PaymentEntity(
            reservation = reservation,
            price = reservation.seat.price,
            paymentStatus = PaymentStatus.SUCCESS,
            paymentType = PaymentType.POINT
        )
        return paymentRepository.save(payment)
    }
}
