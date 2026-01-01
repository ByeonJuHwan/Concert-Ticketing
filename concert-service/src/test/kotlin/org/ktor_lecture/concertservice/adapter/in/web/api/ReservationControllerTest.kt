package org.ktor_lecture.concertservice.adapter.`in`.web.api

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.concertservice.IntegrationTestBase
import org.ktor_lecture.concertservice.application.port.`in`.ReservationExpiredUseCase
import org.ktor_lecture.concertservice.application.port.out.ConcertWriteRepository
import org.ktor_lecture.concertservice.application.port.out.ReservationRepository
import org.ktor_lecture.concertservice.application.port.out.SeatRepository
import org.ktor_lecture.concertservice.application.service.ConcertWriteService
import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand
import org.ktor_lecture.concertservice.application.service.command.ReserveSeatCommand
import org.ktor_lecture.concertservice.domain.entity.SeatEntity
import org.ktor_lecture.concertservice.domain.status.ReservationStatus
import org.ktor_lecture.concertservice.fixture.ConcertFixtures
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.get

class ReservationControllerTest : IntegrationTestBase() {

    @Autowired
    private lateinit var concertWriteService: ConcertWriteService

    @Autowired
    private lateinit var concertWriteRepository: ConcertWriteRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var reservationExpiredUseCase: ReservationExpiredUseCase

    private var reservationId = 0L

    @BeforeEach
    fun setUp() {
        val user = concertWriteRepository.createUser(ConcertFixtures.createConcertUser(id = null))
        val seat = seatRepository.save(createSeat())

        val command = ReserveSeatCommand (
            seat.id!!,
            user.id!!,
        )
        val reservationInfo = concertWriteService.reserveSeat(command)
        reservationId = reservationInfo.reservationId
    }

    @AfterEach
    fun tearDown() {
        concertWriteRepository.deleteAll()
        concertWriteRepository.deleteAllUser()
        seatRepository.deleteAll()
        reservationRepository.deleteAll()
    }

    @Test
    fun `예약정보를 조회한다`() {
        // when && then
        mockMvc.get("/reservations/$reservationId")
            .andExpect {
                status { isOk() }
                jsonPath("$.seatNo") {value(1)}
            }
    }

    @Test
    fun `예약상태를 만료로 변경하고, 좌석의 상태를 예약가능으로 변겅한다`() {
        val command = ReservationExpiredCommand(reservationId)

        // when
        reservationExpiredUseCase.reservationExpiredAndSeatAvaliable(command)

        // then
        val reservation = reservationRepository.getReservation(reservationId).orElseThrow()
        assertThat(reservation.status).isEqualTo(ReservationStatus.EXPIRED)
    }

    @Test
    fun changeReservationPaid() {
    }

    @Test
    fun changeSeatStatus() {
    }

    @Test
    fun changeReservationPending() {
    }

    @Test
    fun changeSeatTemporarilyAssigned() {
    }

    private fun createSeat(): SeatEntity {
        val concert = ConcertFixtures.createConcert(id = null)
        val savedConcert = concertWriteRepository.saveConcert(concert)

        val concertOption = ConcertFixtures.createConcertOption(id = null, concert = savedConcert)
        val savedConcertOption = concertWriteRepository.saveConcertOption(concertOption)

        val seat = ConcertFixtures.createSeat(id = null, concertOption = savedConcertOption)

        return seat
    }

}