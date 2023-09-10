package com.tset.releasemanager

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = [TestConfig::class, ReleaseManagerController::class, ExceptionHandlerController::class])
class ReleaseManagerControllerIT(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val service: ReleaseManagerServiceImpl
) {

    @BeforeEach
    fun setUp() {
        service.versionedServices = ConcurrentHashMap<Int, Set<ServiceDto>>()
        service.services = ConcurrentHashMap<String, Int>()
        service.i = AtomicInteger(0)
    }

    @Test
    fun `should fail when service is deployed without service name`() {
        val result: ErrorResponse = mockMvc.perform(
            post("/api/v1/services/deploy").content(
                ServiceRequest(
                    serviceName = null,
                    serviceVersionNumber = 1
                ).toJsonString()
            ).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .contentAsString
            .toJson()

        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals("Service name should not be null or blank", result.message)
    }

    @Test
    fun `should fail when service is deployed without service version number`() {
        val result: ErrorResponse = mockMvc.perform(
            post("/api/v1/services/deploy").content(
                ServiceRequest(
                    serviceName = "Service A",
                    serviceVersionNumber = null
                ).toJsonString()
            ).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .contentAsString
            .toJson()

        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals("Service version number should not be null", result.message)
    }

    @Test
    fun `should fail when service is deployed with service version number less than 1`() {
        val result: ErrorResponse = mockMvc.perform(
            post("/api/v1/services/deploy").content(
                ServiceRequest(
                    serviceName = "Service A",
                    serviceVersionNumber = 0
                ).toJsonString()
            ).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andReturn()
            .response
            .contentAsString
            .toJson()

        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals("Service version number should not be less than 1", result.message)
    }

    @Test
    fun `deploy one service and get response with system version number`() {
        val result: String = mockMvc.perform(
            post("/api/v1/services/deploy").content(
                ServiceRequest(
                    serviceName = "Service A",
                    serviceVersionNumber = 1
                ).toJsonString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        assertEquals("1", result)
    }

    @Test
    fun `try to deploy service that already exists, should return existing system version`() {
        service.deployService(
            ServiceRequest(
                serviceName = "Service A",
                serviceVersionNumber = 1
            )
        )

        service.deployService(
            ServiceRequest(
                serviceName = "Service B",
                serviceVersionNumber = 1
            )
        )

        service.deployService(
            ServiceRequest(
                serviceName = "Service A",
                serviceVersionNumber = 2
            )
        )

        val result: String = mockMvc.perform(
            post("/api/v1/services/deploy")
                .content(
                    ServiceRequest(
                        serviceName = "Service B",
                        serviceVersionNumber = 1
                    ).toJsonString()
                ).contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        assertEquals("3", result)
    }

    @Test
    fun `deploy two services and check the latest possible system version`() {
        service.deployService(
            ServiceRequest(
                serviceName = "Service A",
                serviceVersionNumber = 1
            )
        )

        service.deployService(
            ServiceRequest(
                serviceName = "Service B",
                serviceVersionNumber = 1
            )
        )

        val result: List<ServiceDto> = mockMvc.perform(
            get("/api/v1/services?systemVersion=2")
        )
            .andExpect(status().isOk)
            .andReturn()
            .response.contentAsString.toJsonAsList()

        assertEquals(
            listOf(
                ServiceDto(
                    serviceName = "Service A",
                    serviceVersionNumber = 1
                ),
                ServiceDto(
                    serviceName = "Service B",
                    serviceVersionNumber = 1
                )
            ), result
        )
    }
}

inline fun <reified T> String.toJson(): T {
    val objectMapper = ObjectMapper().registerKotlinModule()
    objectMapper.registerModule(JavaTimeModule())
    return objectMapper.readValue(this)
}

inline fun <reified T> String.toJsonAsList(): List<T> {
    val objectMapper = ObjectMapper().registerKotlinModule()
    return objectMapper.readValue(this)
}

inline fun <reified T> T.toJsonString(): String {
    val objectMapper = ObjectMapper().registerKotlinModule()
    return objectMapper.writeValueAsString(this)
}