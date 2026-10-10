package com.shopflow.orders.common.exception;

/**
 * Нарушение бизнес-правила. Код ошибки уходит клиенту в поле {@code code}
 * (см. docs/api/openapi.yaml, схема ErrorResponse).
 */
public class BusinessException extends RuntimeException {

	private final String code;

	public BusinessException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
