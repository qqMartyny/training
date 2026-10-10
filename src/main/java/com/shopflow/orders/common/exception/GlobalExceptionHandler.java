package com.shopflow.orders.common.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
		return ResponseEntity.unprocessableContent().body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		return ResponseEntity.badRequest().body(ErrorResponse.of("VALIDATION_ERROR", "Request is invalid", details));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
		return ResponseEntity.badRequest().body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error("Unexpected error", ex);
		return ResponseEntity.internalServerError().body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected error"));
	}

}
