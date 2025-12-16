package org.ktor_lecture.userservice.adapter.`in`.web.api

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktor_lecture.userservice.IntegrationTestBase
import org.ktor_lecture.userservice.adapter.`in`.web.request.CreateUserRequest
import org.ktor_lecture.userservice.application.port.out.UserReadRepository
import org.ktor_lecture.userservice.common.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@Transactional
class UserControllerTest : IntegrationTestBase() {

    @Autowired
    private lateinit var userReadRepository: UserReadRepository

    @Test
    fun `createUser_유저 생성 테스트`() {
        // given
        val request = CreateUserRequest (
            name = "test"
        )

        // when
        mockMvc.post("/api/v1/user") {
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtil.encodeToJson(request)
        }.andExpect { status { isOk() } }

        // then
        val users = userReadRepository.findAll()
        Assertions.assertThat(users.size).isEqualTo(1)
    }

}