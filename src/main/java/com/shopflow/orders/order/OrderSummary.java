package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummary(UUID id, OrderStatus status, BigDecimal total, Instant createdAt) {

	static OrderSummary from(Order order) {
		return new OrderSummary(order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt());
	}

}
