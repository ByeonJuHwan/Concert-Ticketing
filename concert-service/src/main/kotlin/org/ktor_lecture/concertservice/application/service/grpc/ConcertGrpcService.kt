package org.ktor_lecture.concertservice.application.service.grpc

import com.concert.concert.grpc.ConcertServiceGrpcKt
import com.concert.concert.grpc.GrpcChangeReservationPaidRequest
import com.concert.concert.grpc.GrpcChangeReservationPaidResponse
import com.concert.concert.grpc.GrpcChangeSeatReservedRequest
import com.concert.concert.grpc.GrpcChangeSeatReservedResponse
import com.concert.concert.grpc.GrpcConcertReservationResponse
import com.concert.concert.grpc.GrpcGetReservationRequest
import com.concert.concert.grpc.GrpcReservationExpiredAndSeatAvaliableRequest
import com.concert.concert.grpc.GrpcReservationExpiredAndSeatAvaliableResponse
import com.concert.concert.grpc.grpcChangeReservationPaidResponse
import com.concert.concert.grpc.grpcChangeSeatReservedResponse
import com.concert.concert.grpc.grpcConcertReservationResponse
import com.concert.concert.grpc.grpcReservationExpiredAndSeatAvaliableResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import org.ktor_lecture.concertservice.application.service.ConcertReservationService
import org.ktor_lecture.concertservice.application.service.ConcertSeatService
import org.ktor_lecture.concertservice.application.service.command.ChangeSeatStatusReservedCommand
import org.ktor_lecture.concertservice.application.service.command.ReservationExpiredCommand
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


    /**
     * gGPC: Concert-Service 예약 정보를 Payment-Service 로 전달합니다
     */
    override suspend fun getReservation(request: GrpcGetReservationRequest): GrpcConcertReservationResponse {
        log.info("gGPC: 예약 정보 조회 요청 : reservationId=${request.reservationId}")

        return try {
            val reservation = concertReservationService.getReservation(request.reservationId)
            grpcConcertReservationResponse {
                this.reservationId = reservation.reservationId
                this.userId = reservation.userId
                this.seatId = reservation.seatId
                this.seatNo = reservation.seatNo
                this.status = reservation.status
                this.price = reservation.price
                this.expiresAt = reservation.expiresAt.toString()
            }
        } catch (e: ConcertException) {
            log.error("예약 정보 조회 실패 reservationId: {}", request.reservationId, e)
            throw StatusException (
                Status.NOT_FOUND.withDescription(e.message).withCause(e)
            )
        } catch (e: Exception) {
            log.error("서버 에러 발생", e)
            throw StatusException(
                Status.INTERNAL
                    .withDescription("예약 정보 조회 중 오류가 발생했습니다")
                    .withCause(e)
            )
        }
    }

    /**
     * gGPC: Concert-Service 의 만료된 예약을 만료상태로 변경하고 좌석을 다시 예약 가능 상태로 변경합니다
     */
    override suspend fun reservationExpiredAndSeatAvaliable(request: GrpcReservationExpiredAndSeatAvaliableRequest): GrpcReservationExpiredAndSeatAvaliableResponse {
        log.info("gGPC: 예약 만료 상태 변경 및 좌석 예약 가능 상태 변경 요청 : reservationId=${request.reservationId}")

        val command = ReservationExpiredCommand(
            reservationId = request.reservationId
        )
        
        try {
            concertReservationService.reservationExpiredAndSeatAvaliable(command)

            return grpcReservationExpiredAndSeatAvaliableResponse {
                success = true
                message = "예약 만료 상태 변경 및 좌석 예약 가능 변경"
            }
        } catch (e: ConcertException) {
            log.error("예약 만료 상태 변경 및 좌석 예약 가능 상태 변경 실패 reservationId: {}", request.reservationId, e)

            when (e.errorCode) {
                ErrorCode.RESERVATION_NOT_FOUND -> {
                    throw StatusException (
                        Status.NOT_FOUND.withDescription(e.message).withCause(e)
                    )
                }
                else -> throw StatusException (
                    Status.INTERNAL.withDescription(e.message).withCause(e)
                )
            }
        } catch (e: Exception) {
            log.error("서버 에러 발생", e)
            throw StatusException(
                Status.INTERNAL.withDescription(e.message).withCause(e)
            )
        }
    }

    /**
     * gGPC: Concert-Service 의 예약 상태를 결제 완료로 변경합니다
     */
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

            when (e.errorCode) {
                ErrorCode.RESERVATION_NOT_FOUND -> {
                    throw StatusException(
                        Status.NOT_FOUND.withDescription(e.message).withCause(e)
                    )
                }
                else -> throw StatusException(
                    Status.INTERNAL.withDescription(e.message).withCause(e)
                )
            }
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
        log.info("gRPC 좌석 상태 확정 변경 요청 : ${request.reservationId}")

        val command = ChangeSeatStatusReservedCommand (
            requestId = request.reservationId,
        )

        return try {
            concertSeatService.changeSeatStatusReserved(command)

            grpcChangeSeatReservedResponse {
                success = true
                message = "좌석 상태 예약 변경 성공"
            }
        } catch (e: ConcertException) {
            log.error("좌석 예약 상태 변경 비즈니스 예외 발생", e)

            when (e.errorCode) {
                ErrorCode.RESERVATION_NOT_FOUND -> throw StatusException(
                    Status.NOT_FOUND.withDescription(e.message).withCause(e)
                )

                ErrorCode.SEAT_NOT_FOUND -> throw StatusException(
                    Status.NOT_FOUND.withDescription(e.message).withCause(e)
                )

                ErrorCode.SEAT_NOT_TEMPORARILY_ASSIGNED -> throw StatusException (
                    Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e)
                )

                else -> throw StatusException(
                    Status.INTERNAL.withDescription(e.message).withCause(e)
                )
            }
        } catch (e: Exception) {
            log.error("좌석 예약 상태변경 예외 발생", e)
            throw ConcertException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}