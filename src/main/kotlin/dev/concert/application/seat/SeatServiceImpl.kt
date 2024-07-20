package dev.concert.application.seat

import dev.concert.domain.repository.SeatRepository
import dev.concert.domain.entity.SeatEntity
import dev.concert.domain.entity.status.SeatStatus
import dev.concert.exception.NotFoundSeatException
import dev.concert.exception.SeatIsNotAvailableException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SeatServiceImpl(
    private val seatRepository : SeatRepository,
) : SeatService {

    @Transactional(readOnly = true)
    override fun getSeat(seatId: Long): SeatEntity {
        return seatRepository.getSeatWithLock(seatId) ?: throw NotFoundSeatException("해당 좌석이 존재하지 않습니다.")
    }

    override fun checkSeatAvailable(seat: SeatEntity) {
        if (seat.seatStatus != SeatStatus.AVAILABLE){
            throw SeatIsNotAvailableException("해당 좌석은 이미 예약되었습니다.")
        }
    }

    @Transactional
    override fun saveSeat(seatEntity: SeatEntity): SeatEntity {
        return seatRepository.save(seatEntity)
    }

    @Transactional
    override fun changeSeatStatusTemporary(seat: SeatEntity) {
        seat.changeSeatStatus(SeatStatus.TEMPORARILY_ASSIGNED)
        seatRepository.save(seat)
    }

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
}