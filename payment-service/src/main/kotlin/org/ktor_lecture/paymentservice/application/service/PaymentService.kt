package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.application.port.`in`.PaymentCreateUseCase
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUserCreateUseCase
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.application.service.command.PaymentCreateCommand
import org.ktor_lecture.paymentservice.domain.entity.PaymentEntity
import org.ktor_lecture.paymentservice.domain.entity.PaymentStatus
import org.ktor_lecture.paymentservice.domain.entity.PaymentType
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent
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
    override fun save(command: PaymentCreateCommand): String {
        val payment = PaymentEntity(
            price = command.price,
            paymentStatus = PaymentStatus.SUCCESS,
            paymentType = PaymentType.POINT,
        )

        paymentRepository.save(payment)

        return payment.paymentStatus.toString()
    }
}