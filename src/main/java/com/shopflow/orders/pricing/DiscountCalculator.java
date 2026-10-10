package com.shopflow.orders.pricing;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class DiscountCalculator {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	public BigDecimal calculate(PromoCode promo, BigDecimal subtotal) {
		if ("PERCENT".equals(promo.getType())) {
			return subtotal.multiply(promo.getValue()).divide(HUNDRED, Money.SCALE, Money.ROUNDING);
		}
		else {
			// фиксированная скидка в рублях
			return Money.of(promo.getValue());
		}
	}

}
