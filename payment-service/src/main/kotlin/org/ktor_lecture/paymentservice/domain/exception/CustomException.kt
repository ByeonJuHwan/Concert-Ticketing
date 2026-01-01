package org.ktor_lecture.paymentservice.domain.exception

class ConcertException(val errorCode: ErrorCode, customMessage: String? = null) : RuntimeException(customMessage ?: errorCode.message)
