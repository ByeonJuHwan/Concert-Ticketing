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
import dev.concert.domain.repository.PointHistoryRepository
import dev.concert.domain.repository.PointRepository
import dev.concert.domain.repository.ReservationRepository
import dev.concert.domain.exception.NotEnoughPointException
import dev.concert.domain.exception.ReservationAlreadyPaidException
import dev.concert.domain.exception.ReservationExpiredException
import dev.concert.domain.exception.ReservationNotFoundException
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
 
    @Transactional
    override fun processReservationPayment(reservationId: Long) : PaymentEntity {
        // 예약 정보 조회
        val reservation = reservationRepository.findById(reservationId)?: throw ReservationNotFoundException("예약 정보를 찾을 수 없습니다.")

        // 이미 결재한 예약인지 확인한다 (이미 결재 혹은 만료된 예약은 예외를 발생시킨다)
        checkReservationStatus(reservation)

        // 임시 좌석 저장인 5분 안에 결재 요청을 했는지 확인한다 expiresAt 으로 확인(만료되었다면 상태를 Expired로 변경하고 예외를 터트린다)
        isExpired(reservation)

        val user = reservation.user
        val price = reservation.seat.price

        // 포인트 상태를 확인한다 (포인트가 price 보다 적으면 예외를 터트린다)
        val currentPoint = checkPoint(user, price)
        // 포인트 차감 -> 결제를 진행한다 (카드 무통장등등 많지만 요구사항은 포인트 이므로 포인트로 진행)
        currentPoint.deduct(price)
        // 포인트 차감 히스토리 저장
        savePointUseHistory(user, price)

        // 예약 상태 변경 -> 결제 완료로 변경
        reservation.changeStatus(ReservationStatus.PAID)

        // 좌석 상태 변경 -> 예약 완료로 변경
        reservation.seat.changeSeatStatus(SeatStatus.RESERVED)

        // 예약 정보 저장
        return createPayments(reservation)
    }

    private fun savePointUseHistory(user: UserEntity, price: Long) {
        val history = createHistoryEntity(user, price, PointTransactionType.USE)
        pointHistoryRepository.saveHistory(history)
    }

    private fun checkPoint(user: UserEntity, price: Long) : PointEntity {
        val currentPoint = pointRepository.findByUser(user) ?: PointEntity(user, 0)
        if(currentPoint.point < price){
            throw NotEnoughPointException("포인트가 부족합니다.")
        }
        return currentPoint
    }

    private fun isExpired(reservation: ReservationEntity) {
        if(LocalDateTime.now().isAfter(reservation.expiresAt)){
            reservation.changeStatus(ReservationStatus.EXPIRED)
            reservation.seat.changeSeatStatus(SeatStatus.AVAILABLE)
            throw ReservationExpiredException("예약이 만료되었습니다.")
        }
    }

    private fun checkReservationStatus(reservation: ReservationEntity) {
        when (reservation.status) {
            ReservationStatus.PAID -> throw ReservationAlreadyPaidException("이미 결제된 예약입니다.")
            ReservationStatus.EXPIRED -> throw ReservationExpiredException("만료된 예약입니다.")
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
