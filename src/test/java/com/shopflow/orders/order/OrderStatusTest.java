package com.shopflow.orders.order;

import java.math.BigDecimal;

import com.shopflow.orders.payment.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTest {

	@ParameterizedTest
	@CsvSource({ "NEW, PAID", "NEW, AWAITING_PAYMENT", "AWAITING_PAYMENT, PAID", "AWAITING_PAYMENT, PAYMENT_FAILED",
			"PAID, SHIPPED", "PAID, CANCELLED", "SHIPPED, DELIVERED" })
	void allowsTransition(OrderStatus from, OrderStatus to) {
		assertThat(from.canTransitionTo(to)).isTrue();
	}

	@ParameterizedTest
	@CsvSource({ "DELIVERED, CANCELLED", "SHIPPED, CANCELLED", "SHIPPED, PAID", "CANCELLED, PAID",
			"PAYMENT_FAILED, PAID", "DELIVERED, PAID" })
	void forbidsTransition(OrderStatus from, OrderStatus to) {
		assertThat(from.canTransitionTo(to)).isFalse();
	}

	@Test
	void orderRejectsIllegalTransition() {
		Order order = new Order(1L, PaymentMethod.CARD, null, new BigDecimal("100.00"), BigDecimal.ZERO,
				BigDecimal.ZERO);
		order.changeStatus(OrderStatus.PAID);
		order.changeStatus(OrderStatus.SHIPPED);
		order.changeStatus(OrderStatus.DELIVERED);

		assertThatThrownBy(() -> order.changeStatus(OrderStatus.CANCELLED))
			.isInstanceOf(IllegalStatusTransitionException.class);
		assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
	}

	@Test
	void orderKeepsAllItems() {
		Order order = new Order(1L, PaymentMethod.CARD, null, new BigDecimal("300.00"), BigDecimal.ZERO,
				BigDecimal.ZERO);

		order.addItem(1001L, "a", new BigDecimal("100.00"), 1, BigDecimal.ZERO);
		order.addItem(1002L, "b", new BigDecimal("100.00"), 1, BigDecimal.ZERO);
		order.addItem(1003L, "c", new BigDecimal("100.00"), 1, BigDecimal.ZERO);

		assertThat(order.getItems()).hasSize(3);
	}

}
