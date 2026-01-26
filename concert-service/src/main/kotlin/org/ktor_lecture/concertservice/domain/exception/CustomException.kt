package org.ktor_lecture.concertservice.domain.exception

class ConcertException(val errorCode: ErrorCode, val customMessage: String? = null, override val cause: Throwable? = null) : RuntimeException(customMessage ?: errorCode.message)