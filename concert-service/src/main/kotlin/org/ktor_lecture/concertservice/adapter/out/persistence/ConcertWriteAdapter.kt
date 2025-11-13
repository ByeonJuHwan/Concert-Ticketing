package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertUserJpaRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.springframework.stereotype.Component

@Component
class ConcertWriteAdapter (
    private val concertUserJpaRepository: ConcertUserJpaRepository,
): ConcertWriteRepository {

    override fun createUser(user: ConcertUserEntity) {
        concertUserJpaRepository.save(user)
    }
}