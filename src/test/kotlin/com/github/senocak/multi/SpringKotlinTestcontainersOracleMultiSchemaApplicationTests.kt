package com.github.senocak.multi

import com.github.dockerjava.api.command.CreateContainerCmd
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.oracle.OracleContainer
import org.testcontainers.utility.MountableFile.forClasspathResource
import java.time.Duration
import java.util.function.Consumer
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnabledIfDockerAvailable
class MultiSchemaIntegrationFailSetup : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=jdbc:oracle:thin:@//localhost:${oracleContainer.getMappedPort(1521)}/FREEPDB1",
            "spring.datasource.username=USER_SCHEMA",
            "spring.datasource.password=testpassword",
            "spring.datasource.ddl=verify",
            "spring.jpa.hibernate.ddl-auto=none",
        ).applyTo(configurableApplicationContext.environment)
    }

    companion object {
        private val oracleContainer: OracleContainer = OracleContainer("gvenzl/oracle-free:slim-faststart")
            .withDatabaseName("testDB")
            .withUsername("testUser")
            .withPassword("testPassword")
            .withDatabaseName("testDB")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withInitScripts(
                "migrations/V1_create_schemas.sql",
                "migrations/V1__create_users_and_roles.sql"
            )

        init {
            oracleContainer.start()
            oracleContainer.copyFileToContainer(
                forClasspathResource("migrations/V1_create_schemas.sql"),
                "/tmp/V1_create_schemas.sql"
            )
            oracleContainer.copyFileToContainer(
                forClasspathResource("migrations/V1__create_users_and_roles.sql"),
                "/tmp/V1__create_users_and_roles.sql"
            )
            try {
                oracleContainer.execInContainer("./setPassword.sh", "testpassword")
                oracleContainer.execInContainer("sqlplus",
                    "sys/testpassword@FREEPDB1",
                    "as",
                    "sysdba",
                    "@/tmp/V1_create_schemas.sql"
                )
                oracleContainer.execInContainer("sqlplus",
                    "USER_SCHEMA/testpassword@FREEPDB1",
                    "@/tmp/V1__create_users_and_roles.sql"
                )
            } catch (e: Exception) {
                println(message = "Error initializing Oracle UDB container: ${e.localizedMessage}")
                throw RuntimeException("Failed to initialize Oracle UDB container", e)
            }
        }
    }

    @LocalServerPort var localPort: Int? = 0
    private var template: TestRestTemplate? = null

    @BeforeEach
    fun setup() {
        template = TestRestTemplate(RestTemplateBuilder().rootUri("http://localhost:$localPort"))
    }

    @Test
    fun given_whenGetAllUsers_thenAssertResult() {
        // When
        val response = template!!.exchange("/users", HttpMethod.GET, null,
            object : ParameterizedTypeReference<List<User>>() {})
        // Then
        assertNotNull(response.body)
        assertEquals(expected = HttpStatus.OK, actual = response.statusCode)
        assertEquals(expected = 2, actual = response.body!!.size)
    }

    @Test
    fun given_whenGetAllRoles_thenAssertResult() {
        // When
        val response = template!!.exchange("/roles", HttpMethod.GET, null,
            object : ParameterizedTypeReference<List<Address>>() {})
        // Then
        assertNotNull(response.body)
        assertEquals(expected = HttpStatus.OK, actual = response.statusCode)
        assertEquals(expected = 2, actual = response.body!!.size)
    }
}


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnabledIfDockerAvailable
class MultiSchemaIntegrationSuccessSetup {
    companion object {
        @Container
        @JvmStatic
        val oracleContainer: GenericContainer<*> =
            GenericContainer("container-registry.oracle.com/database/free:latest")
                .withExposedPorts(1521)
                .withStartupTimeout(Duration.ofMinutes(2))
                .withCreateContainerCmdModifier(Consumer { cmd: CreateContainerCmd? -> cmd!!.withName("oracle_" + System.currentTimeMillis()) })
                .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE!*\\n", 1))

        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            oracleContainer.copyFileToContainer(
                forClasspathResource("migrations/V1_create_schemas.sql"),
                "/tmp/V1_create_schemas.sql"
            )
            oracleContainer.copyFileToContainer(
                forClasspathResource("migrations/V1__create_users_and_roles.sql"),
                "/tmp/V1__create_users_and_roles.sql"
            )
            try {
                oracleContainer.execInContainer("./setPassword.sh", "testpassword")
                oracleContainer.execInContainer("sqlplus",
                    "sys/testpassword@FREEPDB1",
                    "as",
                    "sysdba",
                    "@/tmp/V1_create_schemas.sql"
                )
                oracleContainer.execInContainer("sqlplus",
                    "USER_SCHEMA/testpassword@FREEPDB1",
                    "@/tmp/V1__create_users_and_roles.sql"
                )
            } catch (e: Exception) {
                println(message = "Error initializing Oracle UDB container: ${e.localizedMessage}")
                throw RuntimeException("Failed to initialize Oracle UDB container", e)
            }

            registry.add("spring.datasource.url") {
                "spring.datasource.url=jdbc:oracle:thin:@//localhost:${oracleContainer.getMappedPort(1521)}/FREEPDB1"
            }
            registry.add("spring.datasource.username") { "USER_SCHEMA" }
            registry.add("spring.datasource.password") { "testpassword" }
            registry.add("spring.datasource.ddl") { "verify" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
        }
    }

    @LocalServerPort var localPort: Int? = 0
    private var template: TestRestTemplate? = null

    @BeforeEach
    fun setup() {
        template = TestRestTemplate(RestTemplateBuilder().rootUri("http://localhost:$localPort"))
    }

    @Test
    fun given_whenGetAllUsers_thenAssertResult() {
        // When
        val response = template!!.exchange("/users", HttpMethod.GET, null,
            object : ParameterizedTypeReference<List<User>>() {})
        // Then
        assertNotNull(response.body)
        assertEquals(expected = HttpStatus.OK, actual = response.statusCode)
        assertEquals(expected = 2, actual = response.body!!.size)
    }

    @Test
    fun given_whenGetAllRoles_thenAssertResult() {
        // When
        val response = template!!.exchange("/roles", HttpMethod.GET, null,
            object : ParameterizedTypeReference<List<Address>>() {})
        // Then
        assertNotNull(response.body)
        assertEquals(expected = HttpStatus.OK, actual = response.statusCode)
        assertEquals(expected = 2, actual = response.body!!.size)
    }
}
