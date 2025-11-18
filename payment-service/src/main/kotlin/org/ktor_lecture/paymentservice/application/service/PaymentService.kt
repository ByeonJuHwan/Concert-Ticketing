package org.ktor_lecture.paymentservice.application.service

import org.ktor_lecture.paymentservice.adapter.`in`.web.response.PaymentResponse
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUseCase
import org.ktor_lecture.paymentservice.application.port.`in`.PaymentUserCreateUseCase
import org.ktor_lecture.paymentservice.application.port.out.PaymentRepository
import org.ktor_lecture.paymentservice.application.port.out.PointApiClient
import org.ktor_lecture.paymentservice.application.port.out.ReservationApiClient
import org.ktor_lecture.paymentservice.application.service.command.PaymentCommand
import org.ktor_lecture.paymentservice.domain.entity.PaymentUserEntity
import org.ktor_lecture.paymentservice.domain.event.UserCreatedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService (
    private val paymentRepository: PaymentRepository,
): PaymentUseCase, PaymentUserCreateUseCase {

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
    override fun pay(command: PaymentCommand): PaymentResponse {
        return PaymentResponse(
            reservationId = TODO(),
            seatNo = TODO(),
            status = TODO(),
            price = TODO()
        )
    }


    @Transactional
    override fun createUser(event: UserCreatedEvent) {
        val user = PaymentUserEntity(
            name = event.userName,
        )

        paymentRepository.createUser(user)
    }
}