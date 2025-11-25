package org.ktor_lecture.userservice.application.port.`in`.point

import org.ktor_lecture.userservice.application.service.command.PointCancelCommand

interface PointCancelUseCase {
    fun cancel(toCommand: PointCancelCommand)

}