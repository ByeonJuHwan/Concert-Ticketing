package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.application.port.`in`.http.PaymentCreateUseCase
import org.ktor_lecture.paymentservice.application.port.`in`.http.PaymentUserCreateUseCase
import org.ktor_lecture.paymentservice.application.port.out.IdempotencyRepository
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.domain.entity.IdempotencyEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentStatus
import org.ktor_lecture.paymentservice.domain.entity.PaymentType
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent
import org.ktor_lecture.paymentservice.domain.exception.ConcertException
import org.ktor_lecture.paymentservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService (
    private val paymentRepository: PaymentRepository,
    private val idempotencyRepository: IdempotencyRepository,
): PaymentUserCreateUseCase, PaymentCreateUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun createUser(event: UserCreatedEvent) {
        val user = PaymentUserEntity(
            name = event.userName,
        )

        paymentRepository.createUser(user)
    }

    @Transactional
    override fun cancelPayment(paymentId: Long, sagaId: String) {
        val idempotency = idempotencyRepository.findBySagaId(sagaId)
        if (idempotency != null) {
            log.info("이미 요청된 결제 취소 상태 변경 요청입니다 sagaId=$sagaId")
            return
        }

        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { throw ConcertException(ErrorCode.PAYMENT_NOT_FOUND) }

        payment.cancel()

        idempotencyRepository.save(
            IdempotencyEntity(
                sagaId = sagaId,
            )
        )
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