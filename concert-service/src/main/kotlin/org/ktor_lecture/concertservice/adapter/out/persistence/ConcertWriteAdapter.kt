package org.ktor_lecture.concertservice.adapter.out.persistence

import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertJpaRepository
import org.ktor_lecture.concertservice.adapter.out.persistence.jpa.ConcertUserJpaRepository
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.springframework.stereotype.Component

@Component
class ConcertWriteAdapter (
    private val concertUserJpaRepository: ConcertUserJpaRepository,
    private val concertJpaRepository: ConcertJpaRepository,
): ConcertWriteRepository {

    override fun createUser(user: ConcertUserEntity) {
        concertUserJpaRepository.save(user)
    }

    override fun saveConcert(concert: ConcertEntity): ConcertEntity {
        return concertJpaRepository.save(concert)
    }
}