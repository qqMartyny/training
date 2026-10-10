package com.shopflow.orders.payment;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentFeeCalculatorTest {

	private final PaymentFeeCalculator calculator = new PaymentFeeCalculator();

	@Test
	void cardHasNoFee() {
		assertThat(calculator.calculate(PaymentMethod.CARD, new BigDecimal("1000.00"))).isEqualByComparingTo("0");
	}

	@Test
	void sbpHasNoFee() {
		assertThat(calculator.calculate(PaymentMethod.SBP, new BigDecimal("1000.00"))).isEqualByComparingTo("0");
	}

}
