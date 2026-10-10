package com.shopflow.orders.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
class FixedAmountDiscountPolicy implements DiscountPolicy {

	@Override
	public DiscountType type() {
		return DiscountType.FIXED_AMOUNT;
	}

	@Override
	public BigDecimal discount(BigDecimal subtotal, BigDecimal value) {
		return value;
	}

}
