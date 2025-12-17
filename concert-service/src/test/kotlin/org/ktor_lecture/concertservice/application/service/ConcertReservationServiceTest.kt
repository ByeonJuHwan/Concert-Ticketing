package org.ktor_lecture.concertservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.service.command.ChangeReservationPendingCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcert
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcertOption
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcertUser
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createReservation
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createSeat
import java.util.*

@ExtendWith(MockKExtension::class)
class ConcertReservationServiceTest {

    @MockK
    private lateinit var reservationRepository: ReservationRepository

    @InjectMockKs
    private lateinit var concertReservationService: ConcertReservationService

    @Test
    fun `getReservation_내 예약 정보를 조회한다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        every { reservationRepository.getReservationWithSeatInfo(reservationId) } returns reservation

        // when
        val result = concertReservationService.getReservation(reservationId)

        // then
        assertThat(result.status).isEqualTo(ReservationStatus.PENDING.toString())
        assertThat(result.reservationId).isEqualTo(reservationId)
        assertThat(result.price).isEqualTo(100L)
    }

    @Test
    fun `getReservation_예약정보 조회 실패`() {
        // given
        val reservationId = 1L

        every { reservationRepository.getReservationWithSeatInfo(reservationId) }.returns(null)

        // when && then
        assertThrows<ConcertException> {
            concertReservationService.getReservation(reservationId)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.RESERVATION_NOT_FOUND)
        }
    }

    @Test
    fun `예약상태 EXPIRED 변경 및 좌석 상태 AVAILABLE`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ReservationExpiredCommand(reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)

        // when
        concertReservationService.reservationExpiredAndSeatAvaliable(command)

        // then
        assertThat(reservation.status).isEqualTo(ReservationStatus.EXPIRED)
        assertThat(reservation.seat.seatStatus).isEqualTo(SeatStatus.AVAILABLE)
    }

    @Test
    fun `예약 상태를 PAID 로 변경한다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ReservationPaidCommand(reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)

        // when
        concertReservationService.changeReservationPaid(command)

        // then
        assertThat(reservation.status).isEqualTo(ReservationStatus.PAID)
    }

    @Test
    fun `예약 상태를 PENDING 으로 변경한다`() {
        // given
         val user = createConcertUser()
         val concert = createConcert()
         val concertOptionEntity = createConcertOption(concert = concert)
         val seat = createSeat(concertOption = concertOptionEntity)

         val reservationId = 1L
         val reservation = createReservation(id = reservationId, user = user, seat = seat)

         val command = ChangeReservationPendingCommand(reservationId)

         every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)

         // when
         concertReservationService.changeReservationPending(command)

         // then
         assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
    }

}