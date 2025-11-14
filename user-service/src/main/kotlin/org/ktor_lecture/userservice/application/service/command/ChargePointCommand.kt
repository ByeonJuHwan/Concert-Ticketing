package org.ktor_lecture.userservice.application.service.command

data class ChargePointCommand(
    val userId : Long,
    val amount : Long,
)
