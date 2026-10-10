package com.shopflow.orders.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
class PercentageDiscountPolicy implements DiscountPolicy {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	@Override
	public DiscountType type() {
		return DiscountType.PERCENTAGE;
	}

	@Override
	public BigDecimal discount(BigDecimal subtotal, BigDecimal value) {
		return subtotal.multiply(value).divide(HUNDRED, Money.SCALE, Money.ROUNDING);
	}

}
