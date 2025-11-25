package org.ktor_lecture.userservice.application.port.`in`.point

import org.ktor_lecture.userservice.application.service.command.PointUseCommand

interface PointUseUseCase {
    fun use(toCommand: PointUseCommand)
}