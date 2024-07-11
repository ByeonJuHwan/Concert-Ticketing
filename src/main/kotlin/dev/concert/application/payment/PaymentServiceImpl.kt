package dev.concert.application.payment

import dev.concert.domain.PaymentRepository
import dev.concert.domain.entity.PaymentEntity
import dev.concert.domain.entity.ReservationEntity
import dev.concert.domain.entity.status.PaymentStatus
import dev.concert.domain.entity.status.PaymentType
import dev.concert.exception.ReservationAlreadyPaidException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service 
class PaymentServiceImpl( 
    private val paymentRepository : PaymentRepository 
) : PaymentService { 
 
    @Transactional 
    override fun createPayments(reservation: ReservationEntity) { 
        val payment = PaymentEntity( 
            reservation = reservation, 
            price = reservation.seat.price, 
            paymentStatus = PaymentStatus.SUCCESS, 
            paymentType = PaymentType.POINT  
        ) 
        paymentRepository.save(payment) 
    } 
 
    override fun checkPayment(reservation: ReservationEntity) { 
        if(paymentRepository.existsByReservation(reservation)) { 
            throw ReservationAlreadyPaidException("이미 결제된 예약입니다.") 
        } 
    } 
} 
