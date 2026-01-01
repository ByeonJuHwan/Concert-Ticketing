package org.ktor_lecture.concertservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.command.ChangeReservationTemporarilyAssignedCommand
import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcert
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcertOption
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcertUser
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createReservation
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createSeat
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class ConcertSeatServiceTest {

    @MockK
    private lateinit var seatRepository: SeatRepository

    @MockK
    private lateinit var reservationRepository: ReservationRepository

    @InjectMockKs
    private lateinit var concertSeatService: ConcertSeatService

    @Test
    fun `임시예약 상태인 좌석의 상태를 예약상태로 변경한다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity, seatStatus = SeatStatus.TEMPORARILY_ASSIGNED)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ChangeSeatStatusReservedCommand (reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)
        every { seatRepository.getSeatWithLock(reservation.seat.id!!) } returns seat

        // when
        concertSeatService.changeSeatStatusReserved(command)

        // then
        assertThat(seat.seatStatus).isEqualTo(SeatStatus.RESERVED)
    }

    @Test
    fun `임시예약 상태가 아닌 좌석의 상태를 예약으로 변경하려고하면 예외를 발생시킨다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ChangeSeatStatusReservedCommand (reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)
        every { seatRepository.getSeatWithLock(reservation.seat.id!!) } returns seat

        // when && then
        assertThrows<ConcertException> {
            concertSeatService.changeSeatStatusReserved(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.SEAT_NOT_TEMPORARILY_ASSIGNED)
        }
    }

    @Test
    fun `예약 정보가 없다면 예외를 발생시킨다`() {
        // given
        val reservationId = 1L

        val command = ChangeSeatStatusReservedCommand (reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.empty()

        // when && then
        assertThrows<ConcertException> {
            concertSeatService.changeSeatStatusReserved(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.RESERVATION_NOT_FOUND)
        }
    }

    @Test
    fun `좌석 정보가 없다면 예외를 발생시킨다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ChangeSeatStatusReservedCommand (reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)
        every { seatRepository.getSeatWithLock(reservation.seat.id!!) } returns null

        // when && then
        assertThrows<ConcertException> {
            concertSeatService.changeSeatStatusReserved(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.SEAT_NOT_FOUND)
        }
    }

    @Test
    fun `예약 좌석의 상태를 임시 예약 상태로 변경한다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity, seatStatus = SeatStatus.RESERVED)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ChangeReservationTemporarilyAssignedCommand (reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)
        every { seatRepository.getSeatWithLock(reservation.seat.id!!) } returns seat

        // when
        concertSeatService.changeSeatTemporarilyAssigned(command)

        // then
        assertThat(seat.seatStatus).isEqualTo(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    @Test
    fun `예약 상태가 아닌 좌석을 임시예약 상태로 변경하려고 하면 예외를 발생시킨다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservationId = 1L
        val reservation = createReservation(id = reservationId, user = user, seat = seat)

        val command = ChangeReservationTemporarilyAssignedCommand (reservationId)

        every { reservationRepository.getReservation(reservationId) } returns Optional.of(reservation)
        every { seatRepository.getSeatWithLock(reservation.seat.id!!) } returns seat

        // when && then
        assertThrows <ConcertException> {
            concertSeatService.changeSeatTemporarilyAssigned(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.SEAT_NOT_RESERVED)
        }
    }

    @Test
    fun `예약 만료 기간이 지난 예약의 상태는 만료로 변경하고 좌석의 상태는 다시 예약 가능으로 변경한다`() {
        // given
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity, seatStatus = SeatStatus.TEMPORARILY_ASSIGNED)

        val reservationId = 1L
        val expiredReservations = listOf(createReservation(id = reservationId, user = user, seat = seat, expiresAt = LocalDateTime.now().minusMinutes(10L)))
        val reservationIds = expiredReservations.map { it.id!! }
        val seatIds = expiredReservations.map { it.seat.id!! }

        every { reservationRepository.findExpiredReservations() } returns expiredReservations
        every { reservationRepository.updateReservationStatusToExpired(reservationIds) } just runs
        every { seatRepository.updateSeatStatusToAvailable(seatIds) } just runs

        // when
        concertSeatService.seatReservationAvailable()

        // then
        verify(exactly = 1) { reservationRepository.updateReservationStatusToExpired(reservationIds)}
        verify(exactly = 1) { seatRepository.updateSeatStatusToAvailable(seatIds)}
    }
}