package org.ktor_lecture.paymentservice.application.service.saga

object SagaType {
    const val PAYMENT = "PAYMENT"
}

object PaymentSagaStep {
    const val POINT_USE = "POINT_USE"
    const val RESERVATION_CONFIRM = "RESERVATION_CONFIRM"
    const val SEAT_CONFIRM = "SEAT_CONFIRM"
    const val PAYMENT_SAVE = "PAYMENT_SAVE"
}