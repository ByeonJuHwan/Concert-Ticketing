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
import org.ktor_lecture.concertservice.application.port.out.*
import org.ktor_lecture.concertservice.application.service.command.CreateConcertCommand
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.domain.entity.ConcertEntity
import org.ktor_lecture.concertservice.domain.entity.ReservationEntity
import org.ktor_lecture.concertservice.domain.event.ConcertCreatedEvent
import org.ktor_lecture.concertservice.domain.event.UserCreatedEvent
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.domain.status.SeatStatus
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcert
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcertOption
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createConcertUser
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createReservation
import org.ktor_lecture.concertservice.fixture.ConcertFixtures.createSeat
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
class ConcertWriteServiceTest {

    @MockK
    private lateinit var concertWriteRepository: ConcertWriteRepository

    @MockK
    private lateinit var concertReadRepository: ConcertReadRepository

    @MockK
    private lateinit var seatRepository: SeatRepository

    @MockK
    private lateinit var reservationRepository: ReservationRepository

    @MockK
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var concertWriteService: ConcertWriteService

    @Test
    fun `좌석을 임시 예약한다`() {
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity)

        val reservation = createReservation(user = user, seat = seat)

        every { concertReadRepository.findUserById(user.id!!) } returns Result.success(user)
        every { seatRepository.getSeatWithLock(seat.id!!)} returns seat
        every { reservationRepository.save(any())} returns reservation
        every { eventPublisher.publish(any())} just runs

        val command = ReserveSeatCommand (
            seat.id!!,
            user.id!!,
        )

        // when
        val result = concertWriteService.reserveSeat(command)

        // then
        assertThat(result.status).isEqualTo(ReservationStatus.PENDING)
        verify(exactly = 1) { reservationRepository.save(any(ReservationEntity::class))}
    }

    @Test
    fun `예약가능한 좌석이 아니면 예외를 발생시킨다`() {
        val user = createConcertUser()
        val concert = createConcert()
        val concertOptionEntity = createConcertOption(concert = concert)
        val seat = createSeat(concertOption = concertOptionEntity, seatStatus = SeatStatus.TEMPORARILY_ASSIGNED)

        every { concertReadRepository.findUserById(user.id!!) } returns Result.success(user)
        every { seatRepository.getSeatWithLock(seat.id!!)} returns seat

        val command = ReserveSeatCommand (
            seat.id!!,
            user.id!!,
        )

        // when && then
        assertThrows <ConcertException> {
            concertWriteService.reserveSeat(command)
        }.also {
            assertThat(it.errorCode).isEqualTo(ErrorCode.SEAT_NOT_AVAILABLE)
        }
    }

    @Test
    fun `콘서트 서비스 유저를 생성한다`() {
        every { concertWriteRepository.createUser(any()) } returnsArgument  0

        val event = UserCreatedEvent(
            userId = "1",
            userName = "test",
        )

        // when
        concertWriteService.createUser(event)

        // then
        verify(exactly = 1) {concertWriteRepository.createUser(any())}
    }

    @Test
    fun `콘서트를 생성한다`() {
        val command = CreateConcertCommand(
            concertName = "testConcert",
            singer = "testSinger",
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            reserveStartDate = LocalDate.now(),
            reserveEndDate = LocalDate.now()
        )

        every { concertWriteRepository.saveConcert(any(ConcertEntity::class)) } answers  {
            firstArg<ConcertEntity>()
            ConcertEntity(
                id = 1L,
                concertName = command.concertName,
                singer = command.singer,
                startDate = command.startDate,
                endDate = command.endDate,
                reserveStartDate = command.reserveStartDate,
                reserveEndDate = command.reserveEndDate,
            )
        }
        every { eventPublisher.publish(any()) } just runs

        // when
        concertWriteService.createConcert(command)

        // then
        verify(exactly = 1) { concertWriteRepository.saveConcert(any(ConcertEntity::class))}
        verify(exactly = 1) { eventPublisher.publish(any(ConcertCreatedEvent::class))}
    }

}