package com.shopflow.orders.payment;

import java.math.BigDecimal;

/**
 * Проведение оплаты у платёжного провайдера.
 */
public interface PaymentProcessor {

	PaymentResult process(PaymentRequest request);

	default BigDecimal fee(BigDecimal amount) {
    return BigDecimal.ZERO;
	}

	PaymentMethod method();
}
