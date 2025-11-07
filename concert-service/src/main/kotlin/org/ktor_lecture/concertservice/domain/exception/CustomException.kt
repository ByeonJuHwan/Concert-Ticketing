package org.ktor_lecture.concertservice.domain.exception

class ConcertException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)