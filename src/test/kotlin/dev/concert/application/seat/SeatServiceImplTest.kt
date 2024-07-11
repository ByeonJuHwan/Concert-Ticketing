package dev.concert.application.seat

import dev.concert.domain.SeatRepository
import dev.concert.domain.entity.ConcertEntity
import dev.concert.domain.entity.ConcertOptionEntity
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.status.SeatStatus
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SeatServiceImplTest {

    @Mock
    lateinit var seatRepository: SeatRepository

    @InjectMocks
    lateinit var seatService: SeatServiceImpl

    @Test
    fun `좌석 Id 가 주어지면 좌석정보를 반환한다`() {
        // given
        val seatEntity = stubSeatEntity()

        val seatId  =1L

        given(seatRepository.getSeatWithLock(seatId)).willReturn(seatEntity)
        // when
        val seat = seatService.getSeat(seatId)

        // then
        assertNotNull(seat)
        assertEquals(seatEntity.id, seat.id)
    }

    @Test
    fun `좌석이 주어지면 예약 가능한 좌석인지 확인한다`() {
        // given
        val seatEntity = stubSeatEntity()

        // when
        assertDoesNotThrow {
            seatService.checkSeatAvailable(seatEntity)
        }
    }

    @Test
    fun `좌성정보가 주어지면 좌석의 상태를 임시 예약으로 변경한다`() {
        // given
        val seatEntity = stubSeatEntity()

        // when
        seatService.changeSeatStatusTemporary(seatEntity)

        // then
        assertThat(seatEntity.seatStatus).isEqualTo(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    @Test
    fun `좌성정보 주어지면 좌석의 상태를 예약으로 변경한다`() {
        // given
        val seatEntity = stubSeatEntity()

        // when
        seatService.changeSeatStatusReserved(seatEntity)

        // then
        assertThat(seatEntity.seatStatus).isEqualTo(SeatStatus.RESERVED)
    }

    @Test
    fun `좌성정보 주어지면 좌석의 상태를 Avaliable 로 변경한다`() {
        // given
        val seatEntity = stubSeatEntity()

        // when
        seatService.changeSeatStatusAvailable(seatEntity)

        // then
        assertThat(seatEntity.seatStatus).isEqualTo(SeatStatus.AVAILABLE)
    }


    private fun stubSeatEntity(): SeatEntity {
        val concertOption = ConcertOptionEntity(
            concert = ConcertEntity(
                concertName = "새해 콘서트",
                singer = "에스파",
                startDate = "20241201",
                endDate = "20241201",
                reserveStartDate = "20241201",
                reserveEndDate = "20241201"
            ),
            availableSeats = 50,
            concertTime = "14:00",
            concertVenue = "올림픽공원",
            concertDate = "20241201"
        )
        val seatEntity = SeatEntity(
            concertOption = concertOption,
            seatNo = 1,
            price = 100000,
        )
        return seatEntity
    }
}