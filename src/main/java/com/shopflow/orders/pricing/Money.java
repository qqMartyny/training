package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Правила округления денежных сумм в рублях (см. docs/SPECIFICATION.md, раздел 3).
 */
public final class Money {

	public static final int SCALE = 2;

	public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

	private Money() {
	}

	public static BigDecimal of(BigDecimal amount) {
		return amount.setScale(SCALE, ROUNDING);
	}

	public static BigDecimal of(String amount) {
		return of(new BigDecimal(amount));
	}

}
