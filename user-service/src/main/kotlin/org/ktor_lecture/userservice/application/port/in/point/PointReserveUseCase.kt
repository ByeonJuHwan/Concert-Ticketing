package org.ktor_lecture.userservice.application.port.`in`.point

import org.ktor_lecture.userservice.application.service.command.PointReserveCommand

interface PointReserveUseCase {
    fun tryReserve(toCommand: PointReserveCommand)
}