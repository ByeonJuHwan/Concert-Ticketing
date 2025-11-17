package org.ktor_lecture.tokenservice.domain.exception

class ConcertException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)