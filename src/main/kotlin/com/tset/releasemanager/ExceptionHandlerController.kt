package com.tset.releasemanager

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class ExceptionHandlerController {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleExceptions(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST, ex.message, LocalDateTime.now()))
    }

}