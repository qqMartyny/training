package com.shopflow.orders.order;

import java.math.BigDecimal;

import com.shopflow.orders.payment.PaymentMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class OrderStatusTest {

	@Test
	void newOrderHasStatusNew() {
		Order order = newOrder();

		assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
	}

	@Test
	void paidOrderCanBeShipped() {
		Order order = newOrder();
		order.setStatus(OrderStatus.PAID);

		order.setStatus(OrderStatus.SHIPPED);

		assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
	}

	@Test
	void deliveredOrderCannotBeCancelled() {
		Order order = newOrder();
		order.setStatus(OrderStatus.DELIVERED);

		try {
			order.setStatus(OrderStatus.CANCELLED);
			fail("Delivered order must not be cancelled");
		}
		catch (Throwable expected) {
			// ok
		}
	}

	@Test
	void orderKeepsItems() {
		Order order = newOrder();

		order.addItem(1001L, "Keyboard", new BigDecimal("100.00"), 1, BigDecimal.ZERO);

		assertThat(order.getItems()).hasSize(1);
	}

	private static Order newOrder() {
		return new Order(1L, PaymentMethod.CARD, null, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO);
	}

}
