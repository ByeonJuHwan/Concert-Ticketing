package org.ktor_lecture.userservice.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.ktor_lecture.userservice.application.port.out.UserWriteRepository
import org.ktor_lecture.userservice.application.service.command.CreateUserCommand
import org.ktor_lecture.userservice.domain.entity.UserEntity
import kotlin.test.Test


@ExtendWith(MockKExtension::class)
class UserWriteServiceTest {


    @MockK
    private lateinit var userWriteRepository: UserWriteRepository

    @InjectMockKs
    private lateinit var userWriteService: UserWriteService

    @Test
    fun `유저 회원가입`() {
        // given
        val command = CreateUserCommand(name = "test")
        val savedUser = UserEntity(name = "test")

        every { userWriteRepository.save(any())} returns savedUser

        // when
        userWriteService.createUser(command)

        // then
        verify(exactly = 1) { userWriteRepository.save(any()) }
    }
}