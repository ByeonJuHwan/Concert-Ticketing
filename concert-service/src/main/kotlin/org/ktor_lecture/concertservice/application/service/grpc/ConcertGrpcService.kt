package org.ktor_lecture.concertservice.application.service.grpc

import com.concert.concert.grpc.ConcertServiceGrpcKt
import com.concert.concert.grpc.GrpcChangeReservationPaidRequest
import com.concert.concert.grpc.GrpcChangeReservationPaidResponse
import com.concert.concert.grpc.GrpcChangeSeatReservedRequest
import com.concert.concert.grpc.GrpcChangeSeatReservedResponse
import com.concert.concert.grpc.grpcChangeReservationPaidResponse
import com.concert.concert.grpc.grpcChangeSeatReservedResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.concertservice.application.service.ConcertReservationService
import org.ktor_lecture.concertservice.application.service.ConcertSeatService
import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationPaidCommand
import org.ktor_lecture.concertservice.domain.exception.ConcertException
import org.ktor_lecture.concertservice.domain.exception.ErrorCode
import org.slf4j.LoggerFactory

@GrpcService
class ConcertGrpcService(
    private val concertReservationService: ConcertReservationService,
    private val concertSeatService: ConcertSeatService,
): ConcertServiceGrpcKt.ConcertServiceCoroutineImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun changeReservationPaid(request: GrpcChangeReservationPaidRequest): GrpcChangeReservationPaidResponse {
        log.info("gRPC 예약 결제 상태 변경 요청: reservationId=${request.reservationId}")

        return try {
            val command = ReservationPaidCommand(
                requestId = request.reservationId
            )

            concertReservationService.changeReservationPaid(command)

            grpcChangeReservationPaidResponse {
                success = true
                message = "예약 결제 상태가 성공적으로 변경되었습니다"
            }
        } catch (e: ConcertException) {
            log.error("예약 결제 상태 변경 실패: code=${e.errorCode}, message=${e.message}", e)

            val status = when (e.errorCode) {
                ErrorCode.RESERVATION_NOT_FOUND -> Status.NOT_FOUND
                else -> Status.INVALID_ARGUMENT
            }

            throw StatusException (
                status.withDescription(e.message)
            )
        } catch (e: Exception) {
            log.error("예약 결제 상태 변경 중 예상치 못한 오류", e)
            throw StatusException(
                Status.INTERNAL
                    .withDescription("예약 결제 상태 변경 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }

    override suspend fun changeSeatReserved(request: GrpcChangeSeatReservedRequest): GrpcChangeSeatReservedResponse {
        log.info("gRPC 좌석 상태 변경 요청")

        val command = ChangeSeatStatusReservedCommand (
            requestId = request.requestId,
        )

        return try {
            concertSeatService.changeSeatStatusReserved(command)

            grpcChangeSeatReservedResponse {
                success = true
                message = "좌석 상태 예약 변경 성공"
            }
        } catch (e: ConcertException) {
            log.error("좌석 예약 상태 변경 비즈니스 예외 발생", e)

            val status = when (e.errorCode) {
                ErrorCode.RESERVATION_NOT_FOUND -> Status.NOT_FOUND
                ErrorCode.SEAT_NOT_FOUND -> Status.NOT_FOUND
                else -> Status.INTERNAL
            }

            throw StatusException (
                status.withDescription(e.message)
                    .withCause(e)
            )

        } catch (e: Exception) {
            log.error("좌석 예약 상태변경 예외 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}