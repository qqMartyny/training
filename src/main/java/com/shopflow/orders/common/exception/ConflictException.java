package com.shopflow.orders.common.exception;

/**
 * Операция конфликтует с текущим состоянием ресурса (HTTP 409).
 */
public class ConflictException extends BusinessException {

	public ConflictException(String code, String message) {
		super(code, message);
	}

}
