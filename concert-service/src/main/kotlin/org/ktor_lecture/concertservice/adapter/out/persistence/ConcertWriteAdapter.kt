package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.springframework.stereotype.Component

@Component
class ConcertWriteAdapter (

): ConcertWriteRepository {
}