package com.tset.releasemanager

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorResponse(
    val httpStatus: HttpStatus,
    val message: String?,
    val time: LocalDateTime
)