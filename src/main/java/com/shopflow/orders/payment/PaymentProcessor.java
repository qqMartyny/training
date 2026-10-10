package com.shopflow.orders.payment;

import java.math.BigDecimal;

/**
 * Стратегия проведения оплаты конкретным способом.
 * Новый способ оплаты = новая реализация этого интерфейса, остальной код не меняется.
 */
public interface PaymentProcessor {

	PaymentMethod method();

	PaymentResult process(PaymentRequest request);

	/**
	 * Сервисный сбор, который платит покупатель (см. docs/SPECIFICATION.md, раздел 4).
	 */
	default BigDecimal fee(BigDecimal amount) {
		return BigDecimal.ZERO;
	}

	default boolean requiresPaymentToken() {
		return false;
	}

}
