package dev.concert.domain.service.seat

import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.domain.exception.NotFoundSeatException
import dev.concert.domain.exception.SeatIsNotAvailableException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SeatServiceImpl(
    private val seatRepository : SeatRepository,
) : SeatService {

    @Transactional
    override fun changeSeatStatusReserved(seat: SeatEntity) {
        seat.changeSeatStatus(SeatStatus.RESERVED)
        seatRepository.save(seat)
    }

    @Transactional
    override fun changeSeatStatusAvailable(seat: SeatEntity) {
        seat.changeSeatStatus(SeatStatus.AVAILABLE)
        seatRepository.save(seat)
    }

    @Transactional
    override fun checkAndReserveSeatTemporarily(seatId: Long): SeatEntity {
        return getSeat(seatId).apply {
            changeSeatStatusTemporary(this)
        }
    }

    private fun getSeat(seatId: Long): SeatEntity {
        return seatRepository.getSeatWithLock(seatId) ?: throw NotFoundSeatException("해당 좌석이 존재하지 않습니다.")
    }

    private fun changeSeatStatusTemporary(seat: SeatEntity) {
        checkSeatAvailable(seat)
        seat.changeSeatStatus(SeatStatus.TEMPORARILY_ASSIGNED)
    }

    private fun checkSeatAvailable(seat: SeatEntity) {
        if (seat.seatStatus != SeatStatus.AVAILABLE){
            throw SeatIsNotAvailableException("해당 좌석은 이미 예약되었습니다.")
        }
    }
}
