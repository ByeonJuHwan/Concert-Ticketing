package org.ktor_lecture.concertservice.fixture

import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertOptionEntity
import org.ktor_lecture.concertservice.domain.entity.ConcertUserEntity
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import java.time.LocalDate
import java.time.LocalDateTime

object ConcertFixtures {

    fun createConcert(
        id: Long = 1L,
        concertName: String = "test_concert",
        singer: String = "test_singer",
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusDays(1),
        reserveStartDate: LocalDate = LocalDate.now().minusDays(7),
        reserveEndDate: LocalDate = LocalDate.now().minusDays(1)
    ) = ConcertEntity(
        id = id,
        concertName = concertName,
        singer = singer,
        startDate = startDate,
        endDate = endDate,
        reserveStartDate = reserveStartDate,
        reserveEndDate = reserveEndDate
    )

    fun createConcerts(count: Int = 2) =
        (1..count).map {
            createConcert(
                id = it.toLong(),
                concertName = "concert_$it",
                singer = "singer_$it"
            )
        }

    fun createConcertOption(
        id: Long = 1L,
        concert: ConcertEntity = createConcert(),
        availableSeats: Int = 100,
        concertDate: String = "20251201",
        concertTime: String = "12:00",
        concertVenue: String = "잠실",
    ) = ConcertOptionEntity(
        id = id,
        concert = concert,
        availableSeats = availableSeats,
        concertDate = concertDate,
        concertTime = concertTime,
        concertVenue = concertVenue,
    )

    fun createConcertOptions(count: Int = 2): List<ConcertOptionEntity> {
        val concert = createConcert()
        return (1..count).map {
            createConcertOption(
                id = it.toLong(),
                concert = concert,
            )
        }
    }

    fun createSeat (
        id: Long = 1L,
        concertOption: ConcertOptionEntity = createConcertOption(),
        seatStatus: SeatStatus = SeatStatus.AVAILABLE,
        price: Long = 100L,
        seatNo: Int = 1,
    ): SeatEntity = SeatEntity(
        id = id,
        concertOption = concertOption,
        seatStatus = seatStatus,
        price = price,
        seatNo = seatNo,
    )

    fun createSeats(count: Int = 2): List<SeatEntity> {
        val concert = createConcert()
        val concertOption = createConcertOption(concert = concert)
        return (1..count).map {
            createSeat(
                id = it.toLong(),
                concertOption = concertOption,
                seatNo = it
            )
        }
    }

    fun createConcertUser(): ConcertUserEntity = ConcertUserEntity(1L, "test")

    fun createReservation(
        id: Long = 1L,
        user: ConcertUserEntity = createConcertUser(),
        seat: SeatEntity = createSeat(),
        expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(10L),
        status: ReservationStatus = ReservationStatus.PENDING,
    ): ReservationEntity {
        return ReservationEntity(
            id = id,
            user = user,
            seat = seat,
            expiresAt = expiresAt,
            status = status,
        )
    }
}