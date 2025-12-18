package org.ktor_lecture.concertservice

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
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

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

        @Container
        val elasticsearchContainer: ElasticsearchContainer = ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.0")
        )
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withCommand("sh", "-c",
                "elasticsearch-plugin install analysis-nori && " +
                "/usr/local/bin/docker-entrypoint.sh"
            )
            .withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // MariaDB
            registry.add("spring.datasource.url", mariadbContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mariadbContainer::getUsername)
            registry.add("spring.datasource.password", mariadbContainer::getPassword)
            registry.add("spring.datasource.driver-class-name", mariadbContainer::getDriverClassName)

            // Kafka
            registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
            registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers)
            registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers)

            // Redis
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)

            // Elasticsearch - ?.를 제거
            registry.add("spring.elasticsearch.uris") {
                "http://${elasticsearchContainer.httpHostAddress}"
            }
        }
    }
}