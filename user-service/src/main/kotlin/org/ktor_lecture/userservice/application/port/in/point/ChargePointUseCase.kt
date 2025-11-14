package org.ktor_lecture.userservice.application.port.`in`.point

import org.ktor_lecture.userservice.adapter.`in`.web.response.CurrentPointResponse
import org.ktor_lecture.userservice.application.service.command.ChargePointCommand

interface ChargePointUseCase {
    fun chargePoints(toCommand: ChargePointCommand): CurrentPointResponse
}