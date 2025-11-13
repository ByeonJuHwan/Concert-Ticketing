package org.ktor_lecture.userservice.domain.exception

class ConcertException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)