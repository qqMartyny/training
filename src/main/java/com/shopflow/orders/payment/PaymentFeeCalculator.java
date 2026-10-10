package com.shopflow.orders.payment;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

/**
 * Сервисный сбор, который платит покупатель (см. docs/SPECIFICATION.md, раздел 4).
 */
@Component
public class PaymentFeeCalculator {

	public BigDecimal calculate(PaymentMethod method, BigDecimal amount) {
		return switch (method) {
			case CARD -> BigDecimal.ZERO;
			case SBP -> BigDecimal.ZERO;
		};
	}

}
