package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.application.port.`in`.http.PaymentCreateUseCase
import org.ktor_lecture.paymentservice.application.port.`in`.http.PaymentUserCreateUseCase
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentStatus
import org.ktor_lecture.paymentservice.domain.entity.PaymentType
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService (
    private val paymentRepository: PaymentRepository,
): PaymentUserCreateUseCase, PaymentCreateUseCase {

    @Transactional
    override fun createUser(event: UserCreatedEvent) {
        val user = PaymentUserEntity(
            name = event.userName,
        )

        paymentRepository.createUser(user)
    }

    @Transactional
    override fun cancelPayment(paymentId: Long) {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { throw ConcertException(ErrorCode.PAYMENT_NOT_FOUND) }

        payment.cancel()
    }

    @Transactional
    override fun save(command: PaymentCreateCommand): PaymentEntity {
        val payment = PaymentEntity(
            price = command.price,
            paymentStatus = PaymentStatus.SUCCESS,
            paymentType = PaymentType.POINT,
        )

        return paymentRepository.save(payment)
    }
}