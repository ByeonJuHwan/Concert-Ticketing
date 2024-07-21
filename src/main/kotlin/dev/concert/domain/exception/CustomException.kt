package dev.concert.domain.exception

class ConcertException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)