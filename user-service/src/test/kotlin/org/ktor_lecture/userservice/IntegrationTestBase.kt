package org.ktor_lecture.userservice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class IntegrationTestBase {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    companion object {
        @Container
        val mariadbContainer: MariaDBContainer<*> = MariaDBContainer(DockerImageName.parse("mariadb:10.11"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)

        @Container
        val kafkaContainer: KafkaContainer = KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.0")
        ).withReuse(true)

        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariadbContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mariadbContainer::getUsername)
            registry.add("spring.datasource.password", mariadbContainer::getPassword)
            registry.add("spring.datasource.driver-class-name", mariadbContainer::getDriverClassName)

            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
            registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers)
            registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers)

            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)
        }
    }
}