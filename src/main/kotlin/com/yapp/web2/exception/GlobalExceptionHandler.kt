package com.yapp.web2.exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.RuntimeException
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log: Logger get() = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(value = [Exception::class, RuntimeException::class])
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("handleException", e)
        val response = ErrorResponse.of(e.message)

        return getResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value())
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.error("handleBusinessException", e)
        val response = ErrorResponse.of(e.message)

        return getResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value())
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidException(e: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        log.error("methodArgumentNotValidException", e)
        val errors = mutableListOf<Error>()
        // TODO: message 수정
        val response = ErrorResponse.of(e.message, errors)

        e.bindingResult.allErrors.forEach { errorObject ->
            val error = Error().apply {
                val field = errorObject as FieldError
                this.field = field.field
                this.message = errorObject.defaultMessage
                this.value = errorObject.rejectedValue
            }
            errors.add(error)
        }
        return getResponse(response, HttpStatus.BAD_REQUEST.value())
    }

    fun getResponse(response: ErrorResponse, statusCode: Int): ResponseEntity<ErrorResponse> {
        val headers = HttpHeaders()
        headers.add("Content-Type", "application/json")

        return ResponseEntity(response, headers, statusCode)
    }
}