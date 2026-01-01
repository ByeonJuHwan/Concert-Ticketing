package org.ktor_lecture.userservice.domain.exception

class ConcertException(val errorCode: ErrorCode, customMessage: String? = null) : RuntimeException(customMessage ?: errorCode.message)