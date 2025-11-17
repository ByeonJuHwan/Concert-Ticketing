package org.ktor_lecture.paymentservice.domain.exception

class ConcertException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)