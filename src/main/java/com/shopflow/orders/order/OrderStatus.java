package com.shopflow.orders.order;

import java.util.EnumSet;
import java.util.Set;

/**
 * Жизненный цикл заказа. Допустимые переходы описаны в docs/SPECIFICATION.md, раздел 5.
 */
public enum OrderStatus {

	NEW, AWAITING_PAYMENT, PAID, PAYMENT_FAILED, SHIPPED, DELIVERED, CANCELLED;

	static {
		NEW.allowed = EnumSet.of(AWAITING_PAYMENT, PAID, PAYMENT_FAILED, CANCELLED);
		AWAITING_PAYMENT.allowed = EnumSet.of(PAID, PAYMENT_FAILED, CANCELLED);
		PAID.allowed = EnumSet.of(SHIPPED, CANCELLED);
		SHIPPED.allowed = EnumSet.of(DELIVERED);
		PAYMENT_FAILED.allowed = EnumSet.noneOf(OrderStatus.class);
		DELIVERED.allowed = EnumSet.noneOf(OrderStatus.class);
		CANCELLED.allowed = EnumSet.noneOf(OrderStatus.class);
	}

	private Set<OrderStatus> allowed;

	public boolean canTransitionTo(OrderStatus target) {
		return allowed.contains(target);
	}

	public boolean isFinal() {
		return allowed.isEmpty();
	}

}
