package org.ktor_lecture.userservice.application.port.`in`.point

import org.ktor_lecture.userservice.application.service.command.PointConfirmCommand

interface PointConfirmUseCase {
    fun confirm(command: PointConfirmCommand)

}